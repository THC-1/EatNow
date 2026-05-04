package com.eatnow.backend.merchant.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.merchant.dto.MerchantApplyRequest;
import com.eatnow.backend.merchant.dto.MerchantUpdateRequest;
import com.eatnow.backend.merchant.service.MerchantManageService;
import com.eatnow.backend.merchant.service.MerchantQueryService;
import com.eatnow.backend.merchant.vo.MerchantApplyVo;
import com.eatnow.backend.merchant.vo.MerchantDetailVo;
import com.eatnow.backend.merchant.vo.MerchantListVo;
import com.eatnow.backend.merchant.vo.MerchantProfileVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantQueryService merchantQueryService;
    private final MerchantManageService merchantManageService;

    @GetMapping
    public ApiResponse<List<MerchantListVo>> listMerchants(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) Long stallId,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(merchantQueryService.listMerchants(canteenId, stallId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<MerchantDetailVo> getMerchantDetail(@PathVariable Long id) {
        return ApiResponse.success(merchantQueryService.getMerchantDetail(id));
    }

    @PostMapping("/apply")
    public ApiResponse<MerchantApplyVo> applyMerchant(@Valid @RequestBody MerchantApplyRequest request) {
        return ApiResponse.success(merchantManageService.applyMerchant(request));
    }

    @GetMapping("/me")
    public ApiResponse<MerchantProfileVo> getCurrentMerchantProfile() {
        return ApiResponse.success(merchantManageService.getCurrentMerchantProfile());
    }

    @PutMapping("/me")
    public ApiResponse<MerchantProfileVo> updateCurrentMerchant(@Valid @RequestBody MerchantUpdateRequest request) {
        return ApiResponse.success(merchantManageService.updateCurrentMerchant(request));
    }
}
