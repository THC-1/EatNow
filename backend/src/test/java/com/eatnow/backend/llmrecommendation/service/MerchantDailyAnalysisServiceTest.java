package com.eatnow.backend.llmrecommendation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eatnow.backend.auth.security.AuthenticatedUser;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.llmrecommendation.config.LlmRecommendationProperties;
import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationChatRequest;
import com.eatnow.backend.llmrecommendation.mongo.MongoRecommendationStore;
import com.eatnow.backend.llmrecommendation.vo.LlmRecommendationChatVo;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyMetricsVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantDailyAnalysisServiceTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAnalyzeFallbackAndEmptyRecommendationsWhenLlmAndMongoAreUnavailable() {
        MerchantMapper merchantMapper = mock(MerchantMapper.class);
        LlmRecommendationProperties properties = new LlmRecommendationProperties();
        properties.setEnabled(false);
        properties.setMongoEnabled(false);
        MerchantDailyAnalysisService service = new MerchantDailyAnalysisService(
                properties,
                merchantMapper,
                mockMongoProvider(),
                RestClient.builder(),
                new ObjectMapper()
        );
        Merchant merchant = merchant();
        MerchantDailyMetricsVo metrics = new MerchantDailyMetricsVo();
        metrics.setTodayReviewCount(0);
        metrics.setTodayFeedbackCount(0);
        metrics.setTodayFavoriteCount(0);
        metrics.setTodayWantEatCount(0);
        metrics.setTodayLotteryCount(0);

        when(merchantMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(merchant);
        when(merchantMapper.selectMerchantDailyMetrics(eq(merchant.getId()), any(), any())).thenReturn(metrics);
        when(merchantMapper.selectMerchantPopularDishes(eq(merchant.getId()), eq(5))).thenReturn(List.of());
        when(merchantMapper.selectMerchantMostFeedbackDishes(eq(merchant.getId()), eq(5))).thenReturn(List.of());
        when(merchantMapper.selectMerchantDailyDishMetrics(eq(merchant.getId()), any(), any(), eq(5))).thenReturn(List.of());
        when(merchantMapper.selectMerchantDailyReviewSamples(eq(merchant.getId()), any(), any(), eq(5))).thenReturn(List.of());
        when(merchantMapper.selectMerchantDailyFeedbackSamples(eq(merchant.getId()), any(), any(), eq(5))).thenReturn(List.of());
        authenticate();

        LlmRecommendationChatRequest request = new LlmRecommendationChatRequest();
        request.setMessage("analyze");
        LlmRecommendationChatVo response = service.analyzeToday(request);

        assertThat(response.getAction()).isEqualTo(MerchantDailyAnalysisService.ACTION_ANALYZE);
        assertThat(response.getRecommendations()).isEmpty();
        assertThat(response.getFallbackUsed()).isTrue();
        assertThat(response.getReply()).contains("\u4eca\u65e5\u6682\u65e0\u65b0\u589e\u4e92\u52a8\u6570\u636e");
        verify(merchantMapper).selectMerchantDailyMetrics(eq(merchant.getId()), any(), any());
    }

    private Merchant merchant() {
        Merchant merchant = new Merchant();
        merchant.setId(8L);
        merchant.setUserId(9L);
        merchant.setName("test merchant");
        merchant.setApplyStatus("APPROVED");
        merchant.setStatus("OPEN");
        return merchant;
    }

    private void authenticate() {
        AuthenticatedUser user = new AuthenticatedUser(9L, "merchant", List.of(RoleCode.MERCHANT.name()));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<MongoRecommendationStore> mockMongoProvider() {
        return mock(ObjectProvider.class);
    }
}
