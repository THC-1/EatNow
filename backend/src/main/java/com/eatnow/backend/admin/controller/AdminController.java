package com.eatnow.backend.admin.controller;

import com.eatnow.backend.admin.dto.AdminCanteenCreateRequest;
import com.eatnow.backend.admin.dto.AdminCanteenUpdateRequest;
import com.eatnow.backend.admin.dto.AdminDishStatusUpdateRequest;
import com.eatnow.backend.admin.dto.AdminStallCreateRequest;
import com.eatnow.backend.admin.dto.AdminStallUpdateRequest;
import com.eatnow.backend.admin.dto.MerchantApplicationRejectRequest;
import com.eatnow.backend.admin.service.AdminService;
import com.eatnow.backend.admin.vo.AdminCanteenScoreVo;
import com.eatnow.backend.admin.vo.AdminDishDetailVo;
import com.eatnow.backend.admin.vo.AdminDishItemVo;
import com.eatnow.backend.admin.vo.AdminMerchantApplicationItemVo;
import com.eatnow.backend.admin.vo.AdminPopularDishVo;
import com.eatnow.backend.admin.vo.AdminPopularMerchantVo;
import com.eatnow.backend.admin.vo.AdminReviewItemVo;
import com.eatnow.backend.admin.vo.AdminStatisticsOverviewVo;
import com.eatnow.backend.admin.vo.AdminUserActivityVo;
import com.eatnow.backend.admin.vo.AdminUserDetailVo;
import com.eatnow.backend.admin.vo.AdminUserListItemVo;
import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<PageResult<AdminUserListItemVo>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(adminService.listUsers(role, status, keyword, page, size));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserDetailVo> getUserDetail(@PathVariable Long id) {
        return ApiResponse.success(adminService.getUserDetail(id));
    }

    @PatchMapping("/users/{id}/disable")
    public ApiResponse<Void> disableUser(@PathVariable Long id) {
        adminService.disableUser(id);
        return ApiResponse.success();
    }

    @PatchMapping("/users/{id}/enable")
    public ApiResponse<Void> enableUser(@PathVariable Long id) {
        adminService.enableUser(id);
        return ApiResponse.success();
    }

    @GetMapping("/merchant-applications")
    public ApiResponse<PageResult<AdminMerchantApplicationItemVo>> listMerchantApplications(
            @RequestParam(required = false) String applyStatus,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(adminService.listMerchantApplications(applyStatus, page, size));
    }

    @PatchMapping("/merchant-applications/{merchantId}/approve")
    public ApiResponse<Void> approveMerchantApplication(@PathVariable Long merchantId) {
        adminService.approveMerchantApplication(merchantId);
        return ApiResponse.success();
    }

    @PatchMapping("/merchant-applications/{merchantId}/reject")
    public ApiResponse<Void> rejectMerchantApplication(
            @PathVariable Long merchantId,
            @Valid @RequestBody MerchantApplicationRejectRequest request
    ) {
        adminService.rejectMerchantApplication(merchantId, request);
        return ApiResponse.success();
    }

    @PostMapping("/canteens")
    public ApiResponse<IdResponse> createCanteen(@Valid @RequestBody AdminCanteenCreateRequest request) {
        return ApiResponse.success(new IdResponse(adminService.createCanteen(request)));
    }

    @PutMapping("/canteens/{id}")
    public ApiResponse<Void> updateCanteen(@PathVariable Long id, @Valid @RequestBody AdminCanteenUpdateRequest request) {
        adminService.updateCanteen(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/canteens/{id}")
    public ApiResponse<Void> deleteCanteen(@PathVariable Long id) {
        adminService.deleteCanteen(id);
        return ApiResponse.success();
    }

    @PostMapping("/stalls")
    public ApiResponse<IdResponse> createStall(@Valid @RequestBody AdminStallCreateRequest request) {
        return ApiResponse.success(new IdResponse(adminService.createStall(request)));
    }

    @PutMapping("/stalls/{id}")
    public ApiResponse<Void> updateStall(@PathVariable Long id, @Valid @RequestBody AdminStallUpdateRequest request) {
        adminService.updateStall(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/stalls/{id}")
    public ApiResponse<Void> deleteStall(@PathVariable Long id) {
        adminService.deleteStall(id);
        return ApiResponse.success();
    }

    @GetMapping("/dishes")
    public ApiResponse<PageResult<AdminDishItemVo>> listDishes(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long canteenId,
            @RequestParam(required = false) Long stallId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(adminService.listDishes(merchantId, canteenId, stallId, status, keyword, page, size));
    }

    @GetMapping("/dishes/{id}")
    public ApiResponse<AdminDishDetailVo> getDishDetail(@PathVariable Long id) {
        return ApiResponse.success(adminService.getDishDetail(id));
    }

    @PatchMapping("/dishes/{id}/status")
    public ApiResponse<Void> updateDishStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminDishStatusUpdateRequest request
    ) {
        adminService.updateDishStatus(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/reviews")
    public ApiResponse<PageResult<AdminReviewItemVo>> listReviews(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(adminService.listReviews(targetType, status, keyword, page, size));
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ApiResponse.success();
    }

    @GetMapping("/statistics/overview")
    public ApiResponse<AdminStatisticsOverviewVo> getStatisticsOverview() {
        return ApiResponse.success(adminService.getStatisticsOverview());
    }

    @GetMapping("/statistics/popular-dishes")
    public ApiResponse<List<AdminPopularDishVo>> getPopularDishes(
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(adminService.getPopularDishes(limit));
    }

    @GetMapping("/statistics/popular-merchants")
    public ApiResponse<List<AdminPopularMerchantVo>> getPopularMerchants(
            @RequestParam(required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.success(adminService.getPopularMerchants(limit));
    }

    @GetMapping("/statistics/user-activity")
    public ApiResponse<List<AdminUserActivityVo>> getUserActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(adminService.getUserActivity(startDate, endDate));
    }

    @GetMapping("/statistics/canteen-scores")
    public ApiResponse<List<AdminCanteenScoreVo>> getCanteenScores() {
        return ApiResponse.success(adminService.getCanteenScores());
    }
}
