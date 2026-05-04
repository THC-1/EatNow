package com.eatnow.backend.campus.controller;

import com.eatnow.backend.campus.service.CampusQueryService;
import com.eatnow.backend.campus.vo.CanteenDetailVo;
import com.eatnow.backend.campus.vo.CanteenListVo;
import com.eatnow.backend.campus.vo.StallDetailVo;
import com.eatnow.backend.campus.vo.StallListVo;
import com.eatnow.backend.common.result.ApiResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CampusController {

    private final CampusQueryService campusQueryService;

    @GetMapping("/canteens")
    public ApiResponse<List<CanteenListVo>> listCanteens(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(campusQueryService.listCanteens(campusId, type, status));
    }

    @GetMapping("/canteens/{id}")
    public ApiResponse<CanteenDetailVo> getCanteenDetail(@PathVariable Long id) {
        return ApiResponse.success(campusQueryService.getCanteenDetail(id));
    }

    @GetMapping("/stalls")
    public ApiResponse<List<StallListVo>> listStalls(
            @RequestParam @NotNull(message = "canteenId must not be null") Long canteenId,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(campusQueryService.listStalls(canteenId, status));
    }

    @GetMapping("/stalls/{id}")
    public ApiResponse<StallDetailVo> getStallDetail(@PathVariable Long id) {
        return ApiResponse.success(campusQueryService.getStallDetail(id));
    }
}
