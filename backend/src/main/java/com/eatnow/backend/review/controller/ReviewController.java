package com.eatnow.backend.review.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.review.dto.ReviewCreateRequest;
import com.eatnow.backend.review.service.ReviewService;
import com.eatnow.backend.review.vo.ReviewItemVo;
import com.eatnow.backend.review.vo.ReviewLikeCountVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<IdResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.success(new IdResponse(reviewService.createReview(request)));
    }

    @GetMapping
    public ApiResponse<PageResult<ReviewItemVo>> listReviews(
            @RequestParam @NotBlank(message = "targetType must not be blank") String targetType,
            @RequestParam @NotNull(message = "targetId must not be null") Long targetId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(reviewService.listReviews(targetType, targetId, sortBy, page, size));
    }

    @GetMapping("/me")
    public ApiResponse<PageResult<ReviewItemVo>> listMyReviews(
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(reviewService.listMyReviews(page, size));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<ReviewLikeCountVo> likeReview(@PathVariable Long id) {
        return ApiResponse.success(reviewService.likeReview(id));
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<ReviewLikeCountVo> unlikeReview(@PathVariable Long id) {
        return ApiResponse.success(reviewService.unlikeReview(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ApiResponse.success();
    }
}
