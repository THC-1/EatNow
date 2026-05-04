package com.eatnow.backend.feedback.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.feedback.dto.MerchantImprovementCreateRequest;
import com.eatnow.backend.feedback.service.MerchantImprovementService;
import com.eatnow.backend.feedback.vo.MerchantImprovementItemVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/merchant/improvement-records")
@RequiredArgsConstructor
public class MerchantImprovementController {

    private final MerchantImprovementService merchantImprovementService;

    @PostMapping
    public ApiResponse<IdResponse> createImprovementRecord(@Valid @RequestBody MerchantImprovementCreateRequest request) {
        return ApiResponse.success(new IdResponse(merchantImprovementService.createImprovementRecord(request)));
    }

    @GetMapping
    public ApiResponse<PageResult<MerchantImprovementItemVo>> listImprovementRecords(
            @RequestParam(required = false) Long dishId,
            @RequestParam(required = false) Long feedbackId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(merchantImprovementService.listImprovementRecords(dishId, feedbackId, status, page, size));
    }
}
