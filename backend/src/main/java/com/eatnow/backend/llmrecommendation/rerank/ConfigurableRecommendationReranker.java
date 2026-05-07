package com.eatnow.backend.llmrecommendation.rerank;

import com.eatnow.backend.llmrecommendation.config.LlmRecommendationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigurableRecommendationReranker implements RecommendationReranker {

    private static final String PROVIDER_LOCAL = "local";
    private static final String PROVIDER_OPENAI = "openai";
    private static final String PROVIDER_OPENAI_COMPATIBLE = "openai-compatible";
    private static final String LLM_FAILURE_FALLBACK_REASON = "LLM智能推荐出现故障，已使用本地精排结果。";

    private final LlmRecommendationProperties properties;
    private final LocalRecommendationReranker localRecommendationReranker;
    private final OpenAiCompatibleRecommendationReranker openAiCompatibleRecommendationReranker;

    @Override
    public RerankResult rerank(RerankRequest request) {
        String provider = properties.getProvider() == null ? PROVIDER_LOCAL : properties.getProvider().trim().toLowerCase();
        if (PROVIDER_LOCAL.equals(provider)) {
            return localRecommendationReranker.rerank(request);
        }
        if (!PROVIDER_OPENAI.equals(provider) && !PROVIDER_OPENAI_COMPATIBLE.equals(provider)) {
            if (!request.recommendationRequested()) {
                return localRecommendationReranker.askFallbackResult(
                        request,
                        "我先确认一下：你更想吃什么口味、预算大概多少？"
                );
            }
            return localRecommendationReranker.fallbackResult(
                    request,
                    "LLM智能推荐配置无效，已使用本地精排结果。"
            );
        }
        try {
            return openAiCompatibleRecommendationReranker.rerank(request);
        } catch (RuntimeException exception) {
            log.warn("LLM rerank failed, fallback to local rerank", exception);
            if (!request.recommendationRequested()) {
                return localRecommendationReranker.askFallbackResult(
                        request,
                        "我先确认一下：你更想吃什么口味、预算大概多少？"
                );
            }
            return localRecommendationReranker.fallbackResult(request, LLM_FAILURE_FALLBACK_REASON);
        }
    }
}
