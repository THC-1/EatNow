package com.eatnow.backend.llmrecommendation.controller;

import com.eatnow.backend.common.result.ApiResponse;
import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationChatRequest;
import com.eatnow.backend.llmrecommendation.service.MerchantDailyAnalysisService;
import com.eatnow.backend.llmrecommendation.service.LlmRecommendationService;
import com.eatnow.backend.llmrecommendation.vo.LlmRecommendationChatVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/llm-recommendations")
@RequiredArgsConstructor
public class LlmRecommendationController {

    private final LlmRecommendationService llmRecommendationService;
    private final MerchantDailyAnalysisService merchantDailyAnalysisService;

    @PostMapping("/student-chat")
    public ApiResponse<LlmRecommendationChatVo> studentChat(@Valid @RequestBody LlmRecommendationChatRequest request) {
        return ApiResponse.success(llmRecommendationService.dishChat(request));
    }

    @PostMapping("/merchant-chat")
    public ApiResponse<LlmRecommendationChatVo> merchantChat(@Valid @RequestBody LlmRecommendationChatRequest request) {
        return ApiResponse.success(merchantDailyAnalysisService.analyzeToday(request));
    }
}
