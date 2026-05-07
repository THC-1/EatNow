package com.eatnow.backend.llmrecommendation.controller;

import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationChatRequest;
import com.eatnow.backend.llmrecommendation.service.LlmRecommendationService;
import com.eatnow.backend.llmrecommendation.service.MerchantDailyAnalysisService;
import com.eatnow.backend.llmrecommendation.vo.LlmRecommendationChatVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LlmRecommendationControllerTest {

    @Test
    void merchantChatRoutesToMerchantDailyAnalysis() {
        LlmRecommendationService recommendationService = mock(LlmRecommendationService.class);
        MerchantDailyAnalysisService analysisService = mock(MerchantDailyAnalysisService.class);
        LlmRecommendationController controller = new LlmRecommendationController(recommendationService, analysisService);
        LlmRecommendationChatRequest request = new LlmRecommendationChatRequest();
        request.setMessage("analyze today");
        LlmRecommendationChatVo response = LlmRecommendationChatVo.builder()
                .action(MerchantDailyAnalysisService.ACTION_ANALYZE)
                .recommendations(List.of())
                .build();

        when(analysisService.analyzeToday(request)).thenReturn(response);

        assertThat(controller.merchantChat(request).getData()).isSameAs(response);
        verify(analysisService).analyzeToday(request);
        verifyNoInteractions(recommendationService);
    }

    @Test
    void studentChatUsesUnifiedDishRecommendation() {
        LlmRecommendationService recommendationService = mock(LlmRecommendationService.class);
        MerchantDailyAnalysisService analysisService = mock(MerchantDailyAnalysisService.class);
        LlmRecommendationController controller = new LlmRecommendationController(recommendationService, analysisService);
        LlmRecommendationChatRequest request = new LlmRecommendationChatRequest();
        request.setMessage("recommend");
        LlmRecommendationChatVo response = LlmRecommendationChatVo.builder()
                .action("RECOMMEND")
                .build();

        when(recommendationService.dishChat(request)).thenReturn(response);

        assertThat(controller.studentChat(request).getData()).isSameAs(response);
        verify(recommendationService).dishChat(request);
        verifyNoInteractions(analysisService);
    }
}
