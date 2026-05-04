package com.eatnow.backend.feedback.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.common.result.IdResponse;
import com.eatnow.backend.common.result.PageResult;
import com.eatnow.backend.feedback.dto.FeedbackCreateRequest;
import com.eatnow.backend.feedback.dto.FeedbackReplyRequest;
import com.eatnow.backend.feedback.dto.FeedbackStatusUpdateRequest;
import com.eatnow.backend.feedback.service.FeedbackService;
import com.eatnow.backend.feedback.vo.FeedbackItemVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ApiResponse<IdResponse> createFeedback(@Valid @RequestBody FeedbackCreateRequest request) {
        return ApiResponse.success(new IdResponse(feedbackService.createFeedback(request)));
    }

    @GetMapping
    public ApiResponse<PageResult<FeedbackItemVo>> listMerchantFeedback(
            @RequestParam(required = false) Long dishId,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(50) Integer size
    ) {
        return ApiResponse.success(feedbackService.listMerchantFeedback(dishId, feedbackType, status, page, size));
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<Void> replyFeedback(@PathVariable Long id, @Valid @RequestBody FeedbackReplyRequest request) {
        feedbackService.replyFeedback(id, request);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateFeedbackStatus(@PathVariable Long id, @Valid @RequestBody FeedbackStatusUpdateRequest request) {
        feedbackService.updateFeedbackStatus(id, request);
        return ApiResponse.success();
    }
}
