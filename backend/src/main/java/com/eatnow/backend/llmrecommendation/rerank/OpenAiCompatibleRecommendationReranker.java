package com.eatnow.backend.llmrecommendation.rerank;

import com.eatnow.backend.llmrecommendation.config.LlmRecommendationProperties;
import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OpenAiCompatibleRecommendationReranker {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final LlmRecommendationProperties properties;

    public RecommendationReranker.RerankResult rerank(RecommendationReranker.RerankRequest request) {
        validateConfig();
        String content = callModel(request);
        return parseModelResult(content, request);
    }

    private void validateConfig() {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new IllegalStateException("LLM baseUrl is not configured");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("LLM apiKey is not configured");
        }
        if (!StringUtils.hasText(properties.getModel())) {
            throw new IllegalStateException("LLM model is not configured");
        }
    }

    private String callModel(RecommendationReranker.RerankRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("temperature", 0.2);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt()),
                Map.of("role", "user", "content", buildUserPrompt(request))
        ));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(properties.getLlmTimeoutSeconds());
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        String responseBody = restClientBuilder.baseUrl(trimTrailingSlash(properties.getBaseUrl()))
                .requestFactory(requestFactory)
                .build()
                .post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .body(body)
                .retrieve()
                .body(String.class);
        if (!StringUtils.hasText(responseBody)) {
            throw new IllegalStateException("LLM returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
                throw new IllegalStateException("LLM response does not contain message content");
            }
            return content.asText();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("LLM response is not valid JSON", exception);
        }
    }

    private RecommendationReranker.RerankResult parseModelResult(
            String content,
            RecommendationReranker.RerankRequest request
    ) {
        JsonNode root;
        try {
            root = objectMapper.readTree(stripJsonFence(content));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("LLM content is not valid rerank JSON", exception);
        }

        String action = normalizeAction(root.path("action").isTextual() ? root.path("action").asText() : null, request);
        String reply = root.path("reply").isTextual()
                ? root.path("reply").asText()
                : defaultReply(action);
        MemoryPayload memory = parseMemory(root.path("memory"));
        if (LocalRecommendationReranker.ACTION_ASK.equals(action)) {
            return new RecommendationReranker.RerankResult(
                    LocalRecommendationReranker.ACTION_ASK,
                    reply,
                    List.of(),
                    Map.of(),
                    memory.conversationSummary(),
                    memory.longTermMemory(),
                    memory.shortTermMemory(),
                    false,
                    null
            );
        }

        Map<Long, LlmCandidateDishVo> candidateMap = new LinkedHashMap<>();
        for (LlmCandidateDishVo candidate : request.candidates()) {
            candidateMap.put(candidate.getDishId(), candidate);
        }

        List<LlmCandidateDishVo> ranked = new ArrayList<>();
        Map<Long, String> reasons = new LinkedHashMap<>();
        Set<Long> usedIds = new LinkedHashSet<>();
        Set<String> usedCategories = new LinkedHashSet<>();
        JsonNode items = root.path("items");
        if (!items.isArray()) {
            throw new IllegalStateException("LLM rerank JSON must contain items array");
        }
        for (JsonNode item : items) {
            Long dishId = item.path("dishId").canConvertToLong() ? item.path("dishId").asLong() : null;
            if (dishId == null || usedIds.contains(dishId) || !candidateMap.containsKey(dishId)) {
                continue;
            }
            LlmCandidateDishVo candidate = candidateMap.get(dishId);
            String categoryKey = categoryKey(candidate);
            if (usedCategories.contains(categoryKey)) {
                continue;
            }
            ranked.add(candidate);
            usedIds.add(dishId);
            usedCategories.add(categoryKey);
            String reason = item.path("reason").isTextual() ? item.path("reason").asText() : null;
            reasons.put(dishId, StringUtils.hasText(reason)
                    ? reason
                    : "\u004c\u004c\u004d\u7ed3\u5408\u4f60\u7684\u8f93\u5165\u548c\u5019\u9009\u83dc\u54c1\u4fe1\u606f\u8fdb\u884c\u4e86\u7cbe\u6392\u63a8\u8350\u3002");
            if (ranked.size() >= request.limit()) {
                break;
            }
        }

        request.candidates().stream()
                .filter(candidate -> !usedIds.contains(candidate.getDishId()))
                .filter(candidate -> !usedCategories.contains(categoryKey(candidate)))
                .sorted(Comparator.comparingDouble(LlmCandidateDishVo::getSoftScore).reversed()
                        .thenComparing(candidate -> candidate.getScore() == null ? BigDecimal.ZERO : candidate.getScore(), Comparator.reverseOrder()))
                .limit(Math.max(0, request.limit() - ranked.size()))
                .forEach(candidate -> {
                    ranked.add(candidate);
                    usedCategories.add(categoryKey(candidate));
                    reasons.put(candidate.getDishId(), "\u004c\u004c\u004d\u672a\u8986\u76d6\u8be5\u5019\u9009\uff0c\u5df2\u6309\u672c\u5730\u7cbe\u6392\u8865\u9f50\u3002");
                });

        if (ranked.isEmpty()) {
            throw new IllegalStateException("LLM did not return usable dish ids");
        }
        return new RecommendationReranker.RerankResult(
                LocalRecommendationReranker.ACTION_RECOMMEND,
                reply,
                ranked,
                reasons,
                memory.conversationSummary(),
                memory.longTermMemory(),
                memory.shortTermMemory(),
                false,
                null
        );
    }

    private String systemPrompt() {
        return """
                You are EatNow's concise Chinese dish recommendation assistant.
                If recommendationRequested is false, ask one short follow-up question and do not recommend dishes.
                If recommendationRequested is true, recommend only dishIds from the provided candidates.
                Do not invent dishes, merchants, prices, ratings, or tags.
                Recommend at most the requested limit, and keep final items in different dish categories when possible.
                After recommending, refresh memory with very short summaries.
                Return strict JSON only:
                {"action":"ASK|RECOMMEND","reply":"short Chinese reply","items":[{"dishId":123,"reason":"short Chinese reason"}],"memory":{"conversationSummary":"short Chinese summary","longTermMemory":"stable taste/avoidance memory","shortTermMemory":"current temporary state"}}
                Rank by user intent, profile, category diversity, price fit, and candidate quality.
                """;
    }

    private String buildUserPrompt(RecommendationReranker.RerankRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", request.message());
        payload.put("profileSummary", request.profile() == null ? null : request.profile().getSummary());
        payload.put("longTermMemory", request.profile() == null ? null : request.profile().getLongTermMemory());
        payload.put("shortTermMemory", request.profile() == null ? null : request.profile().getShortTermMemory());
        payload.put("favoriteDishNames", request.profile() == null ? List.of() : request.profile().getFavoriteDishNames());
        payload.put("eatenDishNames", request.profile() == null ? List.of() : request.profile().getEatenDishNames());
        payload.put("highRatedDishNames", request.profile() == null ? List.of() : request.profile().getHighRatedDishNames());
        payload.put("conversationHistory", request.conversationHistory());
        payload.put("limit", request.limit());
        payload.put("recommendationRequested", request.recommendationRequested());
        payload.put("candidates", request.candidates().stream().map(this::candidatePayload).toList());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize LLM rerank prompt", exception);
        }
    }

    private Map<String, Object> candidatePayload(LlmCandidateDishVo candidate) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dishId", candidate.getDishId());
        payload.put("name", candidate.getName());
        payload.put("description", candidate.getDescription());
        payload.put("price", candidate.getPrice());
        payload.put("score", candidate.getScore());
        payload.put("categoryId", candidate.getCategoryId());
        payload.put("categoryName", candidate.getCategoryName());
        payload.put("canteenName", candidate.getCanteenName());
        payload.put("stallName", candidate.getStallName());
        payload.put("merchantName", candidate.getMerchantName());
        payload.put("tags", candidate.getTags());
        payload.put("softScore", candidate.getSoftScore());
        payload.put("currentRecommendReason", candidate.getCurrentRecommendReason());
        return payload;
    }

    private String normalizeAction(String action, RecommendationReranker.RerankRequest request) {
        if (!request.recommendationRequested()) {
            return LocalRecommendationReranker.ACTION_ASK;
        }
        if (LocalRecommendationReranker.ACTION_ASK.equalsIgnoreCase(action)) {
            return LocalRecommendationReranker.ACTION_ASK;
        }
        return LocalRecommendationReranker.ACTION_RECOMMEND;
    }

    private String defaultReply(String action) {
        if (LocalRecommendationReranker.ACTION_ASK.equals(action)) {
            return "你更想吃什么口味，预算大概多少？";
        }
        return "我按你的要求整理了这几道。";
    }

    private MemoryPayload parseMemory(JsonNode memory) {
        if (memory == null || !memory.isObject()) {
            return new MemoryPayload(null, null, null);
        }
        return new MemoryPayload(
                textOrNull(memory.path("conversationSummary")),
                textOrNull(memory.path("longTermMemory")),
                textOrNull(memory.path("shortTermMemory"))
        );
    }

    private String textOrNull(JsonNode node) {
        if (node == null || !node.isTextual() || !StringUtils.hasText(node.asText())) {
            return null;
        }
        return node.asText().trim();
    }

    private String stripJsonFence(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    private String trimTrailingSlash(String baseUrl) {
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
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

    private record MemoryPayload(String conversationSummary, String longTermMemory, String shortTermMemory) {
    }
}
