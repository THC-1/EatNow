package com.eatnow.backend.llmrecommendation.rerank;

import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class LocalRecommendationReranker implements RecommendationReranker {

    public static final String ACTION_ASK = "ASK";
    public static final String ACTION_RECOMMEND = "RECOMMEND";
    public static final String DEFAULT_FALLBACK_REASON = "\u004c\u004c\u004d\u667a\u80fd\u63a8\u8350\u6682\u65f6\u4e0d\u53ef\u7528\uff0c\u5df2\u4f7f\u7528\u672c\u5730\u7cbe\u6392\u7ed3\u679c\u3002";

    @Override
    public RerankResult rerank(RerankRequest request) {
        if (!request.recommendationRequested()) {
            return askFallbackResult(request, "\u6211\u5148\u518d\u786e\u8ba4\u4e00\u4e0b\uff1a\u4f60\u60f3\u5403\u6e05\u6de1\u3001\u91cd\u53e3\uff0c\u8fd8\u662f\u66f4\u5728\u610f\u4ef7\u683c\u548c\u5206\u91cf\uff1f");
        }
        return fallbackResult(request, "\u5f53\u524d\u672a\u542f\u7528\u004c\u004c\u004d\u667a\u80fd\u63a8\u8350\uff0c\u5df2\u4f7f\u7528\u672c\u5730\u7cbe\u6392\u7ed3\u679c\u3002");
    }

    public RerankResult askFallbackResult(RerankRequest request, String reply) {
        return new RerankResult(
                ACTION_ASK,
                reply,
                List.of(),
                Map.of(),
                null,
                request.profile() == null ? null : request.profile().getLongTermMemory(),
                request.profile() == null ? null : request.profile().getShortTermMemory(),
                true,
                "\u672a\u89e6\u53d1\u63a8\u8350\uff0c\u5df2\u8fd4\u56de\u672c\u5730\u8ffd\u95ee\u3002"
        );
    }

    public RerankResult fallbackResult(RerankRequest request, String fallbackReason) {
        List<LlmCandidateDishVo> sorted = request.candidates().stream()
                .sorted(Comparator.comparingDouble(LlmCandidateDishVo::getSoftScore).reversed()
                        .thenComparing(candidate -> candidate.getScore() == null ? BigDecimal.ZERO : candidate.getScore(), Comparator.reverseOrder())
                        .thenComparing(LlmCandidateDishVo::getDishId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<LlmCandidateDishVo> ranked = pickDistinctCategories(sorted, request.limit());
        Map<Long, String> reasons = new LinkedHashMap<>();
        for (LlmCandidateDishVo candidate : ranked) {
            reasons.put(candidate.getDishId(), buildReason(candidate));
        }
        return new RerankResult(
                ACTION_RECOMMEND,
                buildReply(ranked, fallbackReason),
                ranked,
                reasons,
                buildConversationSummary(request),
                request.profile() == null ? null : request.profile().getLongTermMemory(),
                request.profile() == null ? null : request.profile().getShortTermMemory(),
                true,
                fallbackReason
        );
    }

    private List<LlmCandidateDishVo> pickDistinctCategories(List<LlmCandidateDishVo> sorted, int limit) {
        List<LlmCandidateDishVo> ranked = new ArrayList<>();
        Set<String> usedCategories = new LinkedHashSet<>();
        for (LlmCandidateDishVo candidate : sorted) {
            if (ranked.size() >= limit) {
                break;
            }
            String categoryKey = categoryKey(candidate);
            if (usedCategories.contains(categoryKey)) {
                continue;
            }
            ranked.add(candidate);
            usedCategories.add(categoryKey);
        }
        return ranked;
    }

    private String buildReply(List<LlmCandidateDishVo> ranked, String fallbackReason) {
        if (ranked.isEmpty()) {
            return fallbackReason + " \u6682\u65f6\u6ca1\u6709\u627e\u5230\u5408\u9002\u7684\u5728\u552e\u83dc\u54c1\u3002";
        }
        return fallbackReason + " \u6211\u6309\u4f60\u7684\u8f93\u5165\u3001\u753b\u50cf\u504f\u597d\u3001\u8bc4\u5206\u70ed\u5ea6\u548c\u83dc\u54c1\u79cd\u7c7b\u6574\u7406\u4e86\u8fd9\u4e9b\u9009\u62e9\u3002";
    }

    private String buildReason(LlmCandidateDishVo candidate) {
        if (StringUtils.hasText(candidate.getCurrentRecommendReason())) {
            return candidate.getCurrentRecommendReason();
        }
        if (candidate.getScore() != null && candidate.getScore().compareTo(BigDecimal.valueOf(4.5)) >= 0) {
            return "\u8bc4\u5206\u8868\u73b0\u7a33\u5b9a\uff0c\u9002\u5408\u4f5c\u4e3a\u4f18\u5148\u9009\u62e9\u3002";
        }
        if (candidate.getFavoriteCount() != null && candidate.getFavoriteCount() > 0) {
            return "\u6536\u85cf\u548c\u70ed\u5ea6\u8868\u73b0\u4e0d\u9519\uff0c\u9002\u5408\u52a0\u5165\u5907\u9009\u3002";
        }
        if (candidate.getTags() != null && !candidate.getTags().isEmpty()) {
            return "\u6807\u7b7e\u5339\u914d\u5ea6\u8f83\u9ad8\uff0c\u80fd\u8865\u5145\u672c\u8f6e\u63a8\u8350\u7684\u83dc\u54c1\u591a\u6837\u6027\u3002";
        }
        return "\u7efc\u5408\u4ef7\u683c\u3001\u8bc4\u5206\u548c\u5f53\u524d\u5728\u552e\u72b6\u6001\u63a8\u8350\u3002";
    }

    private String buildConversationSummary(RerankRequest request) {
        if (StringUtils.hasText(request.message())) {
            return "\u672c\u8f6e\u7528\u6237\u8981\u6c42\uff1a" + request.message().trim();
        }
        return "\u672c\u8f6e\u5df2\u6309\u7528\u6237\u9700\u6c42\u5b8c\u6210\u83dc\u54c1\u63a8\u8350\u3002";
    }

    private String categoryKey(LlmCandidateDishVo candidate) {
        if (candidate.getCategoryId() != null) {
            return "id:" + candidate.getCategoryId();
        }
        if (StringUtils.hasText(candidate.getCategoryName())) {
            return "name:" + candidate.getCategoryName();
        }
        return "dish:" + candidate.getDishId();
    }
}
