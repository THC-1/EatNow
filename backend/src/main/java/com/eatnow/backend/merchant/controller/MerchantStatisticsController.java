package com.eatnow.backend.merchant.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.merchant.service.MerchantStatisticsService;
import com.eatnow.backend.merchant.vo.MerchantFeedbackDishVo;
import com.eatnow.backend.merchant.vo.MerchantPopularDishVo;
import com.eatnow.backend.merchant.vo.MerchantStatisticsOverviewVo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/merchant/statistics")
@RequiredArgsConstructor
public class MerchantStatisticsController {

    private final MerchantStatisticsService merchantStatisticsService;

    @GetMapping("/overview")
    public ApiResponse<MerchantStatisticsOverviewVo> getOverview() {
        return ApiResponse.success(merchantStatisticsService.getOverview());
    }

    @GetMapping("/popular-dishes")
    public ApiResponse<List<MerchantPopularDishVo>> listPopularDishes(
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(merchantStatisticsService.listPopularDishes(limit));
    }

    @GetMapping("/most-feedback-dishes")
    public ApiResponse<List<MerchantFeedbackDishVo>> listMostFeedbackDishes(
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(merchantStatisticsService.listMostFeedbackDishes(limit));
    }
}
