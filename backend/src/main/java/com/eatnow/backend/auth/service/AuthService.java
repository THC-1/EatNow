package com.eatnow.backend.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.auth.dto.AccountRegisterRequest;
import com.eatnow.backend.auth.dto.AdminLoginRequest;
import com.eatnow.backend.auth.dto.ChangePasswordRequest;
import com.eatnow.backend.auth.dto.PasswordLoginRequest;
import com.eatnow.backend.auth.dto.RefreshTokenRequest;
import com.eatnow.backend.auth.security.JwtTokenProvider;
import com.eatnow.backend.auth.security.JwtTokenType;
import com.eatnow.backend.auth.security.TokenBlacklistService;
import com.eatnow.backend.auth.vo.AccountLoginVo;
import com.eatnow.backend.auth.vo.AdminLoginVo;
import com.eatnow.backend.auth.vo.TokenPairVo;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.enums.UserStatus;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.user.entity.StudentProfile;
import com.eatnow.backend.user.entity.SysUser;
import com.eatnow.backend.user.entity.SysUserRole;
import com.eatnow.backend.user.mapper.StudentProfileMapper;
import com.eatnow.backend.user.mapper.SysRoleMapper;
import com.eatnow.backend.user.mapper.SysUserMapper;
import com.eatnow.backend.user.mapper.SysUserRoleMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public AdminLoginVo adminLogin(AdminLoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()).last("LIMIT 1")
        );
        if (user == null || !StringUtils.hasText(user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        ensureUserEnabled(user);
        if (!hasRole(user.getId(), RoleCode.ADMIN)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "当前账号不是管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        JwtTokenProvider.TokenPair tokenPair = issueTokenPair(user);
        return AdminLoginVo.builder()
                .adminId(user.getId())
                .tokenPair(TokenPairVo.from(tokenPair))
                .build();
    }

    public AccountLoginVo accountPasswordLogin(PasswordLoginRequest request) {
        SysUser user = authenticatePasswordUser(request.getUsername(), request.getPassword());
        JwtTokenProvider.TokenPair tokenPair = issueTokenPair(user);
        return AccountLoginVo.builder()
                .userId(user.getId())
                .roles(loadRoleCodes(user.getId()))
                .tokenPair(TokenPairVo.from(tokenPair))
                .build();
    }

    @Transactional
    public AccountLoginVo accountRegister(AccountRegisterRequest request) {
        String username = request.getUsername().trim();
        ensureUsernameAvailable(username);

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(resolveRegisterNickname(request.getNickname(), username));
        user.setStatus(UserStatus.ACTIVE.name());
        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }

        ensureRole(user.getId(), RoleCode.STUDENT);
        ensureStudentProfile(user.getId());
        JwtTokenProvider.TokenPair tokenPair = issueTokenPair(user);
        return AccountLoginVo.builder()
                .userId(user.getId())
                .roles(loadRoleCodes(user.getId()))
                .tokenPair(TokenPairVo.from(tokenPair))
                .build();
    }

    public TokenPairVo refresh(RefreshTokenRequest request) {
        Claims claims = parseRefreshToken(request.getRefreshToken());
        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户不存在或登录已失效");
        }
        ensureUserEnabled(user);

        tokenBlacklistService.blacklist(request.getRefreshToken(), claims.getExpiration().toInstant());
        return TokenPairVo.from(issueTokenPair(user));
    }

    public void logout(String accessToken, String refreshToken) {
        blacklistIfNecessary(accessToken);
        blacklistIfNecessary(refreshToken);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前账号不支持密码修改");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "原密码错误");
        }
        sysUserMapper.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .set(SysUser::getPasswordHash, passwordEncoder.encode(request.getNewPassword()))
        );
    }

    private JwtTokenProvider.TokenPair issueTokenPair(SysUser user) {
        List<String> roles = loadRoleCodes(user.getId());
        LocalDateTime now = LocalDateTime.now();
        sysUserMapper.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, user.getId())
                        .set(SysUser::getLastLoginAt, now)
        );
        user.setLastLoginAt(now);
        return jwtTokenProvider.generateTokenPair(user, roles);
    }

    private List<String> loadRoleCodes(Long userId) {
        return sysUserMapper.selectRoleCodesByUserId(userId);
    }

    private SysUser authenticatePasswordUser(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username).last("LIMIT 1")
        );
        if (user == null || !StringUtils.hasText(user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        ensureUserEnabled(user);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        return user;
    }

    private void ensureUsernameAvailable(String username) {
        Long duplicateCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }
    }

    private void ensureRole(Long userId, RoleCode roleCode) {
        if (hasRole(userId, roleCode)) {
            return;
        }
        Long roleId = sysRoleMapper.selectIdByRoleCode(roleCode.name());
        if (roleId == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "角色数据缺失: " + roleCode.name());
        }
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(userId);
        sysUserRole.setRoleId(roleId);
        sysUserRoleMapper.insert(sysUserRole);
    }

    private void ensureStudentProfile(Long userId) {
        StudentProfile profile = studentProfileMapper.selectOne(
                new LambdaQueryWrapper<StudentProfile>()
                        .eq(StudentProfile::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (profile != null) {
            return;
        }
        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setUserId(userId);
        studentProfileMapper.insert(studentProfile);
    }

    private String resolveRegisterNickname(String nickname, String username) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        return username;
    }

    private boolean hasRole(Long userId, RoleCode roleCode) {
        return sysUserMapper.countUserRole(userId, roleCode.name()) > 0;
    }

    private void ensureUserEnabled(SysUser user) {
        if (!UserStatus.ACTIVE.name().equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "当前账号已被禁用");
        }
    }

    private Claims parseRefreshToken(String refreshToken) {
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "refreshToken 已失效");
        }
        try {
            return jwtTokenProvider.parseToken(refreshToken, JwtTokenType.REFRESH);
        } catch (JwtException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "refreshToken 非法或已过期");
        }
    }

    private void blacklistIfNecessary(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            Instant expiresAt = claims.getExpiration().toInstant();
            tokenBlacklistService.blacklist(token, expiresAt);
        } catch (JwtException ignored) {
        }
    }
}
