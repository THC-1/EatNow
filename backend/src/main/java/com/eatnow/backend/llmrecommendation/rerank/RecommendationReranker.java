package com.eatnow.backend.llmrecommendation.rerank;

import com.eatnow.backend.llmrecommendation.service.LlmUserProfile;
import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;

import java.util.List;
import java.util.Map;

public interface RecommendationReranker {

    RerankResult rerank(RerankRequest request);

    record RerankRequest(
            String message,
            LlmUserProfile profile,
            List<String> conversationHistory,
            List<LlmCandidateDishVo> candidates,
            int limit,
            boolean recommendationRequested
    ) {
    }

    record RerankResult(
            String action,
            String reply,
            List<LlmCandidateDishVo> rankedCandidates,
            Map<Long, String> reasons,
            String conversationSummary,
            String longTermMemory,
            String shortTermMemory,
            boolean fallbackUsed,
            String fallbackReason
    ) {
    }
}
