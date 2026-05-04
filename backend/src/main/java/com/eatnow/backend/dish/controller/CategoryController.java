package com.eatnow.backend.dish.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.dish.service.DishQueryService;
import com.eatnow.backend.dish.vo.CategoryVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final DishQueryService dishQueryService;

    @GetMapping
    public ApiResponse<List<CategoryVo>> listCategories(@RequestParam(required = false) String type) {
        return ApiResponse.success(dishQueryService.listCategories(type));
    }
}
