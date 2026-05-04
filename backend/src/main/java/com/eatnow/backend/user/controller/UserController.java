package com.eatnow.backend.user.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.user.dto.UserPreferenceUpdateRequest;
import com.eatnow.backend.user.dto.UserProfileUpdateRequest;
import com.eatnow.backend.user.service.UserService;
import com.eatnow.backend.user.vo.UserPreferenceVo;
import com.eatnow.backend.user.vo.UserProfileVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileVo> getCurrentProfile() {
        return ApiResponse.success(userService.getCurrentProfile());
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileVo> updateCurrentProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.success(userService.updateCurrentProfile(request));
    }

    @GetMapping("/preferences")
    public ApiResponse<UserPreferenceVo> getCurrentPreference() {
        return ApiResponse.success(userService.getCurrentPreference());
    }

    @PutMapping("/preferences")
    public ApiResponse<UserPreferenceVo> updateCurrentPreference(@Valid @RequestBody UserPreferenceUpdateRequest request) {
        return ApiResponse.success(userService.upsertCurrentPreference(request));
    }
}
