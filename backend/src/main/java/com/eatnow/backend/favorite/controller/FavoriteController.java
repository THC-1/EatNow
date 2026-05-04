package com.eatnow.backend.favorite.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.favorite.dto.FavoriteCreateRequest;
import com.eatnow.backend.favorite.service.FavoriteService;
import com.eatnow.backend.favorite.vo.FavoriteCheckVo;
import com.eatnow.backend.favorite.vo.FavoriteItemVo;
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
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ApiResponse<IdResponse> createFavorite(@Valid @RequestBody FavoriteCreateRequest request) {
        return ApiResponse.success(new IdResponse(favoriteService.createFavorite(request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFavorite(@PathVariable Long id) {
        favoriteService.deleteFavorite(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<FavoriteItemVo>> listFavorites(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(favoriteService.listFavorites(targetType, page, size));
    }

    @GetMapping("/check")
    public ApiResponse<FavoriteCheckVo> checkFavorite(
            @RequestParam @NotBlank(message = "targetType must not be blank") String targetType,
            @RequestParam @NotNull(message = "targetId must not be null") Long targetId
    ) {
        return ApiResponse.success(favoriteService.checkFavorite(targetType, targetId));
    }
}
