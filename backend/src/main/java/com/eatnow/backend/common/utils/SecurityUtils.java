package com.eatnow.backend.common.utils;

import com.eatnow.backend.auth.security.AuthenticatedUser;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser getCurrentUser() {
        AuthenticatedUser authenticatedUser = getCurrentUserOrNull();
        if (authenticatedUser == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }
        return authenticatedUser;
    }

    public static AuthenticatedUser getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            return null;
        }
        return authenticatedUser;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public static List<String> getCurrentRoleCodes() {
        AuthenticatedUser authenticatedUser = getCurrentUserOrNull();
        if (authenticatedUser == null || authenticatedUser.roles() == null) {
            return Collections.emptyList();
        }
        return authenticatedUser.roles();
    }

    public static boolean hasRole(RoleCode roleCode) {
        return getCurrentRoleCodes().contains(roleCode.name());
    }

    public static void requireRole(RoleCode roleCode) {
        if (!hasRole(roleCode)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权执行当前操作");
        }
    }
}
