package com.eatnow.backend.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.auth.security.JwtTokenProvider;
import com.eatnow.backend.auth.vo.MerchantLoginVo;
import com.eatnow.backend.auth.vo.StudentLoginVo;
import com.eatnow.backend.auth.vo.TokenPairVo;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.enums.UserStatus;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.eatnow.backend.user.entity.StudentProfile;
import com.eatnow.backend.user.entity.SysUser;
import com.eatnow.backend.user.entity.SysUserRole;
import com.eatnow.backend.user.mapper.StudentProfileMapper;
import com.eatnow.backend.user.mapper.SysRoleMapper;
import com.eatnow.backend.user.mapper.SysUserMapper;
import com.eatnow.backend.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WechatLoginService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final MerchantMapper merchantMapper;
    private final WechatAuthClient wechatAuthClient;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public StudentLoginVo miniProgramStudentLogin(String code) {
        SysUser user = loadOrCreateMiniProgramUser(code);
        boolean isNewUser = user.getLastLoginAt() == null;
        ensureRole(user.getId(), RoleCode.STUDENT);
        ensureStudentProfile(user.getId());
        JwtTokenProvider.TokenPair tokenPair = issueTokenPair(user);
        return StudentLoginVo.builder()
                .userId(user.getId())
                .isNewUser(isNewUser)
                .tokenPair(TokenPairVo.from(tokenPair))
                .build();
    }

    @Transactional
    public MerchantLoginVo miniProgramMerchantLogin(String code) {
        SysUser user = loadOrCreateMiniProgramUser(code);
        return buildMerchantLoginVo(user);
    }

    private MerchantLoginVo buildMerchantLoginVo(SysUser user) {
        ensureRole(user.getId(), RoleCode.MERCHANT);
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, user.getId()).last("LIMIT 1")
        );
        JwtTokenProvider.TokenPair tokenPair = issueTokenPair(user);
        return MerchantLoginVo.builder()
                .userId(user.getId())
                .merchantId(merchant == null ? null : merchant.getId())
                .applyStatus(merchant == null ? null : merchant.getApplyStatus())
                .merchantStatus(merchant == null ? null : merchant.getStatus())
                .tokenPair(TokenPairVo.from(tokenPair))
                .build();
    }

    private SysUser loadOrCreateMiniProgramUser(String code) {
        WechatAuthClient.WechatIdentity identity = wechatAuthClient.exchangeMiniProgramCode(code);
        SysUser user = findByMiniProgramOpenid(identity.openid());
        if (user == null) {
            user = createWechatUser(identity.openid());
        } else {
            ensureUserEnabled(user);
        }
        return user;
    }

    private SysUser findByMiniProgramOpenid(String openid) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getWechatOpenid, openid).last("LIMIT 1")
        );
    }

    private SysUser createWechatUser(String openid) {
        SysUser user = new SysUser();
        user.setWechatOpenid(openid);
        user.setNickname(buildDefaultNickname(openid));
        user.setStatus(UserStatus.ACTIVE.name());
        sysUserMapper.insert(user);
        return sysUserMapper.selectById(user.getId());
    }

    private JwtTokenProvider.TokenPair issueTokenPair(SysUser user) {
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());
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

    private boolean hasRole(Long userId, RoleCode roleCode) {
        return sysUserMapper.countUserRole(userId, roleCode.name()) > 0;
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

    private void ensureUserEnabled(SysUser user) {
        if (!UserStatus.ACTIVE.name().equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "当前账号已被禁用");
        }
    }

    private String buildDefaultNickname(String identifier) {
        String suffix = identifier.length() <= 6 ? identifier : identifier.substring(identifier.length() - 6);
        return "微信用户" + suffix;
    }
}
