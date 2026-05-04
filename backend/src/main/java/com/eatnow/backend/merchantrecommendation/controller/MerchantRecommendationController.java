package com.eatnow.backend.merchantrecommendation.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.merchantrecommendation.dto.MerchantRecommendationCreateRequest;
import com.eatnow.backend.merchantrecommendation.dto.MerchantRecommendationUpdateRequest;
import com.eatnow.backend.merchantrecommendation.service.MerchantRecommendationService;
import com.eatnow.backend.merchantrecommendation.vo.MerchantRecommendationItemVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/merchant-recommendations")
@RequiredArgsConstructor
public class MerchantRecommendationController {

    private final MerchantRecommendationService merchantRecommendationService;

    @PostMapping
    public ApiResponse<IdResponse> createRecommendation(@Valid @RequestBody MerchantRecommendationCreateRequest request) {
        return ApiResponse.success(new IdResponse(merchantRecommendationService.createRecommendation(request)));
    }

    @GetMapping
    public ApiResponse<PageResult<MerchantRecommendationItemVo>> listMyRecommendations(
            @RequestParam(required = false) String recommendType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(merchantRecommendationService.listMyRecommendations(recommendType, status, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateRecommendation(
            @PathVariable Long id,
            @Valid @RequestBody MerchantRecommendationUpdateRequest request
    ) {
        merchantRecommendationService.updateRecommendation(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecommendation(@PathVariable Long id) {
        merchantRecommendationService.deleteRecommendation(id);
        return ApiResponse.success();
    }
}
