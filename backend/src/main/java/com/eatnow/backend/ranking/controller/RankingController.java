package com.eatnow.backend.ranking.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.ranking.service.RankingService;
import com.eatnow.backend.ranking.vo.RankingItemVo;
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
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/top-rated")
    public ApiResponse<List<RankingItemVo>> topRated(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(rankingService.topRated(canteenId, categoryId, limit));
    }

    @GetMapping("/popular")
    public ApiResponse<List<RankingItemVo>> popular(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(rankingService.popular(canteenId, limit));
    }

    @GetMapping("/most-favorited")
    public ApiResponse<List<RankingItemVo>> mostFavorited(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(rankingService.mostFavorited(canteenId, limit));
    }

    @GetMapping("/best-value")
    public ApiResponse<List<RankingItemVo>> bestValue(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(rankingService.bestValue(canteenId, limit));
    }

    @GetMapping("/new-dishes")
    public ApiResponse<List<RankingItemVo>> newDishes(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(rankingService.newDishes(canteenId, limit));
    }

    @GetMapping("/most-feedback")
    public ApiResponse<List<RankingItemVo>> mostFeedback(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(rankingService.mostFeedback(canteenId, limit));
    }
}
