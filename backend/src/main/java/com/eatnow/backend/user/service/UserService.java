package com.eatnow.backend.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eatnow.backend.campus.entity.Canteen;
import com.eatnow.backend.campus.mapper.CanteenMapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.user.dto.UserPreferenceUpdateRequest;
import com.eatnow.backend.user.dto.UserProfileUpdateRequest;
import com.eatnow.backend.user.entity.SysUser;
import com.eatnow.backend.user.entity.UserPreference;
import com.eatnow.backend.user.mapper.SysUserMapper;
import com.eatnow.backend.user.mapper.UserPreferenceMapper;
import com.eatnow.backend.user.vo.UserPreferenceVo;
import com.eatnow.backend.user.vo.UserProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String CANTEEN_TYPE_CANTEEN = "CANTEEN";
    private static final String CANTEEN_TYPE_CAMPUS_SHOP = "CAMPUS_SHOP";
    private static final String CANTEEN_TYPE_PERIPHERY_SHOP = "PERIPHERY_SHOP";

    private final SysUserMapper sysUserMapper;
    private final UserPreferenceMapper userPreferenceMapper;
    private final CanteenMapper canteenMapper;

    public UserProfileVo getCurrentProfile() {
        Long userId = requireStudentAndGetUserId();
        SysUser user = getUserOrThrow(userId);
        return toUserProfileVo(user);
    }

    @Transactional
    public UserProfileVo updateCurrentProfile(UserProfileUpdateRequest request) {
        Long userId = requireStudentAndGetUserId();
        SysUser user = getUserOrThrow(userId);
        validateUserProfileUpdateRequest(request);

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId);

        if (request.getNickname() != null) {
            updateWrapper.set(SysUser::getNickname, requireText(request.getNickname(), "nickname"));
        }
        if (request.getAvatar() != null) {
            updateWrapper.set(SysUser::getAvatarUrl, trimToNull(request.getAvatar()));
        }
        if (request.getPhone() != null) {
            String normalizedPhone = trimToNull(request.getPhone());
            ensurePhoneAvailable(userId, normalizedPhone);
            updateWrapper.set(SysUser::getPhone, normalizedPhone);
        }

        sysUserMapper.update(null, updateWrapper);
        return getCurrentProfile();
    }

    public UserPreferenceVo getCurrentPreference() {
        Long userId = requireStudentAndGetUserId();
        UserPreference preference = userPreferenceMapper.selectOne(
                new LambdaQueryWrapper<UserPreference>()
                        .eq(UserPreference::getUserId, userId)
                        .last("LIMIT 1")
        );
        return toUserPreferenceVo(preference, userId);
    }

    @Transactional
    public UserPreferenceVo upsertCurrentPreference(UserPreferenceUpdateRequest request) {
        Long userId = requireStudentAndGetUserId();
        validateUserPreferenceRequest(request);

        UserPreference preference = userPreferenceMapper.selectOne(
                new LambdaQueryWrapper<UserPreference>()
                        .eq(UserPreference::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (preference == null) {
            preference = new UserPreference();
            preference.setUserId(userId);
            applyPreferenceValues(preference, request);
            userPreferenceMapper.insert(preference);
        } else {
            applyPreferenceValues(preference, request);
            userPreferenceMapper.updateById(preference);
        }
        return toUserPreferenceVo(preference, userId);
    }

    private void applyPreferenceValues(UserPreference preference, UserPreferenceUpdateRequest request) {
        preference.setDefaultCanteenId(request.getDefaultCanteenId());
        preference.setDefaultCanteenType(normalizeOptionalCanteenType(request.getDefaultCanteenType()));
        preference.setMinPrice(request.getMinPrice());
        preference.setMaxPrice(request.getMaxPrice());
        preference.setTastePreference(trimToNull(request.getTastePreference()));
        preference.setAvoidTags(trimToNull(request.getAvoidTags()));
    }

    private void validateUserPreferenceRequest(UserPreferenceUpdateRequest request) {
        if (request.getMinPrice() != null && request.getMaxPrice() != null
                && request.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }

        if (request.getDefaultCanteenId() != null) {
            Canteen canteen = canteenMapper.selectById(request.getDefaultCanteenId());
            if (canteen == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "defaultCanteenId is invalid");
            }
            String requestedType = normalizeOptionalCanteenType(request.getDefaultCanteenType());
            if (requestedType != null && !requestedType.equals(canteen.getType())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "defaultCanteenType does not match canteen");
            }
        } else if (StringUtils.hasText(request.getDefaultCanteenType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "defaultCanteenId must not be null when defaultCanteenType is provided");
        }
    }

    private void validateUserProfileUpdateRequest(UserProfileUpdateRequest request) {
        if (request.getNickname() == null && request.getAvatar() == null && request.getPhone() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "update payload must not be empty");
        }
    }

    private void ensurePhoneAvailable(Long userId, String phone) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        Long duplicateCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, phone)
                        .ne(SysUser::getId, userId)
        );
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "phone already exists");
        }
    }

    private Long requireStudentAndGetUserId() {
        SecurityUtils.requireRole(RoleCode.STUDENT);
        return SecurityUtils.getCurrentUserId();
    }

    private SysUser getUserOrThrow(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    private UserProfileVo toUserProfileVo(SysUser user) {
        return UserProfileVo.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserPreferenceVo toUserPreferenceVo(UserPreference preference, Long userId) {
        if (preference == null) {
            return UserPreferenceVo.builder()
                    .userId(userId)
                    .build();
        }
        return UserPreferenceVo.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .defaultCanteenId(preference.getDefaultCanteenId())
                .defaultCanteenType(preference.getDefaultCanteenType())
                .minPrice(preference.getMinPrice())
                .maxPrice(preference.getMaxPrice())
                .tastePreference(preference.getTastePreference())
                .avoidTags(preference.getAvoidTags())
                .build();
    }

    private String normalizeOptionalCanteenType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        String normalized = type.trim().toUpperCase();
        if (!CANTEEN_TYPE_CANTEEN.equals(normalized)
                && !CANTEEN_TYPE_CAMPUS_SHOP.equals(normalized)
                && !CANTEEN_TYPE_PERIPHERY_SHOP.equals(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "defaultCanteenType is invalid");
        }
        return normalized;
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
