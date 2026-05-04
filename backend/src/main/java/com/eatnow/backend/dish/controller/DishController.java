package com.eatnow.backend.dish.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.dish.dto.DishQueryRequest;
import com.eatnow.backend.dish.dto.DishSaveRequest;
import com.eatnow.backend.dish.dto.DishStatusUpdateRequest;
import com.eatnow.backend.dish.service.DishManageService;
import com.eatnow.backend.dish.service.DishQueryService;
import com.eatnow.backend.dish.vo.DishDetailVo;
import com.eatnow.backend.dish.vo.DishListVo;
import com.eatnow.backend.merchantrecommendation.service.MerchantRecommendationService;
import com.eatnow.backend.merchantrecommendation.vo.DishRecommendationVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishQueryService dishQueryService;
    private final DishManageService dishManageService;
    private final MerchantRecommendationService merchantRecommendationService;

    @GetMapping
    public ApiResponse<PageResult<DishListVo>> listDishes(@Valid DishQueryRequest query) {
        return ApiResponse.success(dishQueryService.listDishes(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<DishDetailVo> getDishDetail(@PathVariable Long id) {
        return ApiResponse.success(dishQueryService.getDishDetail(id));
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<DishListVo>> searchDishes(
            @RequestParam @NotBlank(message = "keyword must not be blank") String keyword,
            @RequestParam(required = false) @Min(value = 1, message = "page must be greater than or equal to 1")
            Integer page,
            @RequestParam(required = false) @Min(value = 1, message = "size must be greater than or equal to 1")
            @Max(value = 50, message = "size must be less than or equal to 50") Integer size
    ) {
        return ApiResponse.success(dishQueryService.searchDishes(keyword, page, size));
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<DishRecommendationVo>> listDishRecommendations(
            @RequestParam @NotNull(message = "merchantId must not be null") Long merchantId,
            @RequestParam(required = false) String recommendType
    ) {
        return ApiResponse.success(merchantRecommendationService.listDishRecommendations(merchantId, recommendType));
    }

    @PostMapping
    public ApiResponse<IdResponse> createDish(@Valid @RequestBody DishSaveRequest request) {
        return ApiResponse.success(new IdResponse(dishManageService.createDish(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<DishDetailVo> updateDish(
            @PathVariable Long id,
            @Valid @RequestBody DishSaveRequest request
    ) {
        return ApiResponse.success(dishManageService.updateDish(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateDishStatus(
            @PathVariable Long id,
            @Valid @RequestBody DishStatusUpdateRequest request
    ) {
        dishManageService.updateDishStatus(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDish(@PathVariable Long id) {
        dishManageService.deleteDish(id);
        return ApiResponse.success();
    }
}
