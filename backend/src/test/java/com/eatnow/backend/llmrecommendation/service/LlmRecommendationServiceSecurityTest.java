package com.eatnow.backend.llmrecommendation.service;

import com.eatnow.backend.auth.security.AuthenticatedUser;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.llmrecommendation.config.LlmRecommendationProperties;
import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationChatRequest;
import com.eatnow.backend.llmrecommendation.mapper.LlmRecommendationMapper;
import com.eatnow.backend.llmrecommendation.mongo.MongoRecommendationStore;
import com.eatnow.backend.llmrecommendation.rerank.RecommendationReranker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class LlmRecommendationServiceSecurityTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dishChatAllowsMerchantRoleBeforeRunningRecommendationFlow() {
        LlmRecommendationProperties properties = new LlmRecommendationProperties();
        properties.setEnabled(false);
        LlmRecommendationService service = new LlmRecommendationService(
                properties,
                mock(LlmRecommendationMapper.class),
                mock(LlmRecommendationProfileService.class),
                mock(LlmRecommendationScoringService.class),
                mock(LlmRecommendationDiversitySampler.class),
                mock(RecommendationReranker.class),
                mockMongoProvider()
        );
        LlmRecommendationChatRequest request = new LlmRecommendationChatRequest();
        request.setMessage("recommend");
        authenticate(RoleCode.MERCHANT);

        assertThatThrownBy(() -> service.dishChat(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private void authenticate(RoleCode roleCode) {
        AuthenticatedUser user = new AuthenticatedUser(9L, "merchant", List.of(roleCode.name()));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<MongoRecommendationStore> mockMongoProvider() {
        return mock(ObjectProvider.class);
    }
}
