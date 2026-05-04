package com.eatnow.backend.auth.controller;

import com.eatnow.backend.auth.dto.AdminLoginRequest;
import com.eatnow.backend.auth.dto.AccountRegisterRequest;
import com.eatnow.backend.auth.dto.ChangePasswordRequest;
import com.eatnow.backend.auth.dto.LogoutRequest;
import com.eatnow.backend.auth.dto.PasswordLoginRequest;
import com.eatnow.backend.auth.dto.RefreshTokenRequest;
import com.eatnow.backend.auth.dto.WechatLoginRequest;
import com.eatnow.backend.auth.service.AuthService;
import com.eatnow.backend.auth.service.WechatLoginService;
import com.eatnow.backend.auth.vo.AccountLoginVo;
import com.eatnow.backend.auth.vo.AdminLoginVo;
import com.eatnow.backend.auth.vo.MerchantLoginVo;
import com.eatnow.backend.auth.vo.StudentLoginVo;
import com.eatnow.backend.auth.vo.TokenPairVo;
import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final WechatLoginService wechatLoginService;

    @PostMapping("/student-login")
    public ApiResponse<StudentLoginVo> studentLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(wechatLoginService.miniProgramStudentLogin(request.getCode()));
    }

    @PostMapping("/merchant-login")
    public ApiResponse<MerchantLoginVo> merchantLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(wechatLoginService.miniProgramMerchantLogin(request.getCode()));
    }

    @PostMapping("/android/login")
    public ApiResponse<AccountLoginVo> androidLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return ApiResponse.success(authService.accountPasswordLogin(request));
    }

    @PostMapping("/android/register")
    public ApiResponse<AccountLoginVo> androidRegister(@Valid @RequestBody AccountRegisterRequest request) {
        return ApiResponse.success(authService.accountRegister(request));
    }

    @PostMapping("/admin-login")
    public ApiResponse<AdminLoginVo> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(authService.adminLogin(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenPairVo> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        authService.logout(resolveAccessToken(authorization), request == null ? null : request.getRefreshToken());
        return ApiResponse.success();
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.getCurrentUserId(), request);
        return ApiResponse.success();
    }

    private String resolveAccessToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
