package com.eatnow.backend.llmrecommendation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.llmrecommendation.config.LlmRecommendationProperties;
import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationChatRequest;
import com.eatnow.backend.llmrecommendation.mongo.MongoRecommendationStore;
import com.eatnow.backend.llmrecommendation.vo.LlmRecommendationChatVo;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyAnalysisSnapshot;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyDishMetricVo;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyMetricsVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.mapper.MerchantMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantDailyAnalysisService {

    public static final String ACTION_ANALYZE = "ANALYZE";

    private static final String APPLY_APPROVED = "APPROVED";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String PROVIDER_OPENAI = "openai";
    private static final String PROVIDER_OPENAI_COMPATIBLE = "openai-compatible";
    private static final int LIST_LIMIT = 5;

    private final LlmRecommendationProperties properties;
    private final MerchantMapper merchantMapper;
    private final ObjectProvider<MongoRecommendationStore> mongoStoreProvider;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public LlmRecommendationChatVo analyzeToday(LlmRecommendationChatRequest request) {
        Merchant merchant = getCurrentMerchant();
        MerchantDailyAnalysisSnapshot mysqlSnapshot = buildMysqlSnapshot(merchant);
        MerchantDailyAnalysisSnapshot snapshot = saveAndLoadMongoSnapshot(mysqlSnapshot);
        AnalysisReply analysisReply = analyzeWithFallback(snapshot, request.getMessage());
        return LlmRecommendationChatVo.builder()
                .conversationId(normalizeConversationId(request.getConversationId()))
                .action(ACTION_ANALYZE)
                .reply(analysisReply.reply())
                .recommendations(List.of())
                .fallbackUsed(analysisReply.fallbackUsed())
                .fallbackReason(analysisReply.fallbackReason())
                .conversationSummary(buildConversationSummary(snapshot))
                .memoryUpdated(false)
                .profileUpdatedAt(snapshot.getGeneratedAt())
                .build();
    }

    private Merchant getCurrentMerchant() {
        SecurityUtils.requireRole(RoleCode.MERCHANT);
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, SecurityUtils.getCurrentUserId())
                        .last("LIMIT 1")
        );
        if (merchant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Merchant not found");
        }
        if (!APPLY_APPROVED.equals(merchant.getApplyStatus()) || STATUS_DISABLED.equals(merchant.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Merchant is not approved or is disabled");
        }
        return merchant;
    }

    private MerchantDailyAnalysisSnapshot buildMysqlSnapshot(Merchant merchant) {
        LocalDate reportDate = LocalDate.now();
        LocalDateTime startTime = reportDate.atStartOfDay();
        LocalDateTime endTime = reportDate.plusDays(1).atStartOfDay();

        MerchantDailyAnalysisSnapshot snapshot = new MerchantDailyAnalysisSnapshot();
        snapshot.setMerchantId(merchant.getId());
        snapshot.setUserId(merchant.getUserId());
        snapshot.setMerchantName(merchant.getName());
        snapshot.setReportDate(reportDate);
        snapshot.setGeneratedAt(LocalDateTime.now());
        snapshot.setOverview(merchantMapper.selectMerchantStatisticsOverview(merchant.getId()));
        snapshot.setDailyMetrics(Optional.ofNullable(merchantMapper.selectMerchantDailyMetrics(
                merchant.getId(),
                startTime,
                endTime
        )).orElseGet(MerchantDailyMetricsVo::new));
        snapshot.setPopularDishes(merchantMapper.selectMerchantPopularDishes(merchant.getId(), LIST_LIMIT));
        snapshot.setFeedbackDishes(merchantMapper.selectMerchantMostFeedbackDishes(merchant.getId(), LIST_LIMIT));
        snapshot.setDailyDishMetrics(merchantMapper.selectMerchantDailyDishMetrics(merchant.getId(), startTime, endTime, LIST_LIMIT));
        snapshot.setReviewSamples(merchantMapper.selectMerchantDailyReviewSamples(merchant.getId(), startTime, endTime, LIST_LIMIT));
        snapshot.setFeedbackSamples(merchantMapper.selectMerchantDailyFeedbackSamples(merchant.getId(), startTime, endTime, LIST_LIMIT));
        return snapshot;
    }

    private MerchantDailyAnalysisSnapshot saveAndLoadMongoSnapshot(MerchantDailyAnalysisSnapshot snapshot) {
        Optional<MongoRecommendationStore> store = getMongoStore();
        if (store.isEmpty()) {
            return snapshot;
        }
        try {
            store.get().saveMerchantDailyReport(snapshot);
            return store.get()
                    .findMerchantDailyReport(snapshot.getMerchantId(), snapshot.getReportDate())
                    .map(MongoRecommendationStore.MerchantDailyReportDocument::getSnapshot)
                    .orElse(snapshot);
        } catch (RuntimeException exception) {
            log.warn("Mongo merchant daily report save/read failed, analysis continues from MySQL snapshot", exception);
            return snapshot;
        }
    }

    private AnalysisReply analyzeWithFallback(MerchantDailyAnalysisSnapshot snapshot, String userMessage) {
        String fallbackReply = buildLocalAnalysis(snapshot);
        if (!properties.isEnabled()) {
            return new AnalysisReply(fallbackReply, true, "LLM recommendation module is disabled, used local merchant analysis.");
        }
        if (!isOpenAiCompatibleProvider() || !hasLlmConfig()) {
            return new AnalysisReply(fallbackReply, true, "LLM provider is not configured for merchant analysis, used local merchant analysis.");
        }
        try {
            return new AnalysisReply(callLlm(snapshot, userMessage), false, null);
        } catch (RuntimeException exception) {
            log.warn("LLM merchant daily analysis failed, fallback to local analysis", exception);
            return new AnalysisReply(fallbackReply, true, "LLM merchant analysis failed, used local merchant analysis.");
        }
    }

    private String callLlm(MerchantDailyAnalysisSnapshot snapshot, String userMessage) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("temperature", 0.2);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt()),
                Map.of("role", "user", "content", buildUserPrompt(snapshot, userMessage))
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
            JsonNode content = objectMapper.readTree(responseBody).path("choices").path(0).path("message").path("content");
            if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
                throw new IllegalStateException("LLM response does not contain message content");
            }
            return content.asText().trim();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("LLM response is not valid JSON", exception);
        }
    }

    private String systemPrompt() {
        return """
                You are EatNow's merchant operations analyst.
                Write concise Chinese for the merchant workspace.
                Use only the supplied JSON snapshot. Do not invent orders, revenue, sales volume, GMV, conversion rate, or unavailable business metrics.
                Mention that viewCount, favoriteCount, reviewCount, score, and recommendation click counts are existing platform counters when they are cumulative.
                Output four short sections: \u4eca\u65e5\u6982\u51b5\u3001\u4eae\u70b9\u3001\u98ce\u9669\u3001\u660e\u65e5\u5efa\u8bae.
                """;
    }

    private String buildUserPrompt(MerchantDailyAnalysisSnapshot snapshot, String userMessage) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("merchantQuestion", userMessage);
        payload.put("snapshot", snapshot);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize merchant analysis prompt", exception);
        }
    }

    private String buildLocalAnalysis(MerchantDailyAnalysisSnapshot snapshot) {
        MerchantDailyMetricsVo metrics = Optional.ofNullable(snapshot.getDailyMetrics()).orElseGet(MerchantDailyMetricsVo::new);
        MerchantDailyDishMetricVo topDish = first(snapshot.getDailyDishMetrics());
        String topDishText = topDish == null || dailyDishActivity(topDish) == 0
                ? "\u4eca\u65e5\u6682\u65e0\u660e\u663e\u5355\u54c1\u7206\u70b9\uff0c\u53ef\u5148\u5173\u6ce8\u7d2f\u8ba1\u70ed\u95e8\u83dc\u54c1\u7684\u7a33\u5b9a\u4e0a\u67b6\u3002"
                : String.format("\u4eca\u65e5\u4e92\u52a8\u8f83\u96c6\u4e2d\u5728\u300c%s\u300d\uff0c\u8bc4\u4ef7\u3001\u53cd\u9988\u3001\u60f3\u5403\u548c\u62bd\u7b7e\u547d\u4e2d\u5408\u8ba1 %d \u6b21\u3002",
                topDish.getDishName(), dailyDishActivity(topDish));
        String riskText = intValue(metrics.getTotalPendingFeedbackCount()) > 0
                ? String.format("\u5f53\u524d\u8fd8\u6709 %d \u6761\u5f85\u5904\u7406\u53cd\u9988\uff0c\u5efa\u8bae\u4f18\u5148\u56de\u590d\u5e76\u8bb0\u5f55\u6539\u8fdb\u3002", intValue(metrics.getTotalPendingFeedbackCount()))
                : "\u6682\u65e0\u5f85\u5904\u7406\u53cd\u9988\uff0c\u7ee7\u7eed\u4fdd\u6301\u8bc4\u4ef7\u548c\u53cd\u9988\u54cd\u5e94\u901f\u5ea6\u3002";
        if (intValue(metrics.getTodayFeedbackCount()) == 0 && intValue(metrics.getTodayReviewCount()) == 0
                && intValue(metrics.getTodayFavoriteCount()) == 0 && intValue(metrics.getTodayWantEatCount()) == 0
                && intValue(metrics.getTodayLotteryCount()) == 0) {
            topDishText = "\u4eca\u65e5\u6682\u65e0\u65b0\u589e\u4e92\u52a8\u6570\u636e\uff0c\u672c\u6b21\u5206\u6790\u4ee5\u5b58\u91cf\u83dc\u54c1\u548c\u5f85\u5904\u7406\u53cd\u9988\u4e3a\u4e3b\u3002";
        }
        return String.format("""
                        \u4eca\u65e5\u6982\u51b5\uff1a\u65b0\u589e\u8bc4\u4ef7 %d \u6761\uff0c\u65b0\u589e\u53cd\u9988 %d \u6761\uff0c\u65b0\u589e\u6536\u85cf %d \u6b21\uff0c\u60f3\u5403 %d \u6b21\uff0c\u62bd\u7b7e\u547d\u4e2d %d \u6b21\u3002
                        \u4eae\u70b9\uff1a%s
                        \u98ce\u9669\uff1a%s
                        \u660e\u65e5\u5efa\u8bae\uff1a\u4f18\u5148\u5904\u7406\u5f85\u5904\u7406\u53cd\u9988\uff0c\u4fdd\u6301\u9ad8\u8bc4\u5206\u548c\u9ad8\u4e92\u52a8\u83dc\u54c1\u5728\u552e\uff0c\u5e76\u7528\u4eca\u65e5\u63a8\u8350\u4f4d\u5f3a\u5316\u66dd\u5149\u3002
                        """,
                intValue(metrics.getTodayReviewCount()),
                intValue(metrics.getTodayFeedbackCount()),
                intValue(metrics.getTodayFavoriteCount()),
                intValue(metrics.getTodayWantEatCount()),
                intValue(metrics.getTodayLotteryCount()),
                topDishText,
                riskText
        ).trim();
    }

    private MerchantDailyDishMetricVo first(List<MerchantDailyDishMetricVo> dishes) {
        return dishes == null || dishes.isEmpty() ? null : dishes.get(0);
    }

    private int dailyDishActivity(MerchantDailyDishMetricVo dish) {
        return intValue(dish.getTodayReviewCount())
                + intValue(dish.getTodayFeedbackCount())
                + intValue(dish.getTodayFavoriteCount())
                + intValue(dish.getTodayWantEatCount())
                + intValue(dish.getTodayLotteryCount());
    }

    private Optional<MongoRecommendationStore> getMongoStore() {
        if (!properties.isMongoEnabled()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mongoStoreProvider.getIfAvailable());
    }

    private boolean isOpenAiCompatibleProvider() {
        String provider = properties.getProvider() == null ? "" : properties.getProvider().trim().toLowerCase();
        return PROVIDER_OPENAI.equals(provider) || PROVIDER_OPENAI_COMPATIBLE.equals(provider);
    }

    private boolean hasLlmConfig() {
        return StringUtils.hasText(properties.getBaseUrl())
                && StringUtils.hasText(properties.getApiKey())
                && StringUtils.hasText(properties.getModel());
    }

    private int intValue(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return UUID.randomUUID().toString();
        }
        return conversationId.trim();
    }

    private String buildConversationSummary(MerchantDailyAnalysisSnapshot snapshot) {
        return snapshot.getReportDate() + " merchant daily analysis for " + snapshot.getMerchantName();
    }

    private String trimTrailingSlash(String baseUrl) {
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private record AnalysisReply(String reply, boolean fallbackUsed, String fallbackReason) {
    }
}
