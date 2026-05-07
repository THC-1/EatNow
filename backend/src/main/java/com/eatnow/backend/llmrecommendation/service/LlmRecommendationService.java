package com.eatnow.backend.llmrecommendation.service;

import com.eatnow.backend.common.enums.RoleCode;
import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.common.utils.SecurityUtils;
import com.eatnow.backend.llmrecommendation.config.LlmRecommendationProperties;
import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationChatRequest;
import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationConstraints;
import com.eatnow.backend.llmrecommendation.mapper.LlmRecommendationMapper;
import com.eatnow.backend.llmrecommendation.mongo.MongoRecommendationStore;
import com.eatnow.backend.llmrecommendation.rerank.LocalRecommendationReranker;
import com.eatnow.backend.llmrecommendation.rerank.RecommendationReranker;
import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import com.eatnow.backend.llmrecommendation.vo.LlmDishImageRelationVo;
import com.eatnow.backend.llmrecommendation.vo.LlmDishTagRelationVo;
import com.eatnow.backend.llmrecommendation.vo.LlmRecommendationChatVo;
import com.eatnow.backend.llmrecommendation.vo.LlmRecommendationItemVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmRecommendationService {

    private final LlmRecommendationProperties properties;
    private final LlmRecommendationMapper recommendationMapper;
    private final LlmRecommendationProfileService profileService;
    private final LlmRecommendationScoringService scoringService;
    private final LlmRecommendationDiversitySampler diversitySampler;
    private final RecommendationReranker reranker;
    private final ObjectProvider<MongoRecommendationStore> mongoStoreProvider;

    public LlmRecommendationChatVo dishChat(LlmRecommendationChatRequest request) {
        return chat(request, resolveDishRecommendationRole());
    }

    public LlmRecommendationChatVo studentChat(LlmRecommendationChatRequest request) {
        return dishChat(request);
    }

    public LlmRecommendationChatVo merchantChat(LlmRecommendationChatRequest request) {
        return dishChat(request);
    }

    private String resolveDishRecommendationRole() {
        if (SecurityUtils.hasRole(RoleCode.STUDENT)) {
            return RoleCode.STUDENT.name();
        }
        if (SecurityUtils.hasRole(RoleCode.MERCHANT)) {
            return RoleCode.MERCHANT.name();
        }
        SecurityUtils.requireRole(RoleCode.STUDENT);
        return RoleCode.STUDENT.name();
    }

    private LlmRecommendationChatVo chat(LlmRecommendationChatRequest request, String role) {
        if (!properties.isEnabled()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "LLM recommendation module is disabled");
        }
        validateConstraints(request.getConstraints());
        Long userId = SecurityUtils.getCurrentUserId();
        int limit = normalizeLimit(request.getLimit());
        String conversationId = normalizeConversationId(request.getConversationId());
        boolean recommendationRequested = isRecommendationRequested(request.getMessage());

        LlmUserProfile profile = profileService.getOrRefreshProfile(userId, role);
        List<String> history = loadConversationHistory(conversationId, userId, role);

        List<LlmCandidateDishVo> recalled = recommendationMapper.selectAvailableCandidateDishes(
                request.getConstraints(),
                properties.getRecallSize()
        );
        decorate(recalled);
        if (recalled.isEmpty()) {
            recalled = recommendationMapper.selectPopularCandidateDishes(properties.getRecallSize());
            decorate(recalled);
        }
        if (recalled.isEmpty() && recommendationRequested) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "No on-sale dishes are available for LLM recommendation");
        }

        scoringService.score(recalled, profile, request.getMessage(), request.getConstraints());
        List<LlmCandidateDishVo> popularFillers = recommendationMapper.selectPopularCandidateDishes(properties.getRecallSize());
        decorate(popularFillers);
        scoringService.score(popularFillers, profile, request.getMessage(), request.getConstraints());

        int sampleSize = Math.max(limit, properties.getSampleSize());
        List<LlmCandidateDishVo> sampled = diversitySampler.sample(recalled, popularFillers, sampleSize);
        RecommendationReranker.RerankResult rerankResult = reranker.rerank(
                new RecommendationReranker.RerankRequest(
                        request.getMessage(),
                        profile,
                        history,
                        sampled,
                        limit,
                        recommendationRequested
                )
        );

        List<LlmRecommendationItemVo> recommendations = toRecommendationItems(
                rerankResult.rankedCandidates(),
                rerankResult.reasons()
        );
        boolean memoryUpdated = updateProfileMemory(profile, rerankResult);
        saveConversation(conversationId, userId, role, request.getMessage(), rerankResult.reply(), rerankResult, recommendations);
        return LlmRecommendationChatVo.builder()
                .conversationId(conversationId)
                .action(rerankResult.action())
                .reply(rerankResult.reply())
                .recommendations(recommendations)
                .fallbackUsed(rerankResult.fallbackUsed())
                .fallbackReason(rerankResult.fallbackReason())
                .conversationSummary(rerankResult.conversationSummary())
                .longTermMemory(profile.getLongTermMemory())
                .shortTermMemory(profile.getShortTermMemory())
                .memoryUpdated(memoryUpdated)
                .profileUpdatedAt(profile.getUpdatedAt())
                .build();
    }

    private void decorate(List<LlmCandidateDishVo> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        List<Long> dishIds = candidates.stream().map(LlmCandidateDishVo::getDishId).toList();
        Map<Long, List<String>> imageMap = buildImageMap(dishIds);
        Map<Long, List<String>> tagMap = buildTagMap(dishIds);
        for (LlmCandidateDishVo candidate : candidates) {
            List<String> images = new ArrayList<>(imageMap.getOrDefault(candidate.getDishId(), Collections.emptyList()));
            if (images.isEmpty() && StringUtils.hasText(candidate.getCoverImageUrl())) {
                images.add(candidate.getCoverImageUrl());
            }
            candidate.setImages(images);
            candidate.setTags(tagMap.getOrDefault(candidate.getDishId(), Collections.emptyList()));
        }
    }

    private Map<Long, List<String>> buildImageMap(List<Long> dishIds) {
        Map<Long, List<String>> imageMap = new LinkedHashMap<>();
        for (LlmDishImageRelationVo relation : recommendationMapper.selectDishImages(dishIds)) {
            if (StringUtils.hasText(relation.getImageUrl())) {
                imageMap.computeIfAbsent(relation.getDishId(), key -> new ArrayList<>()).add(relation.getImageUrl());
            }
        }
        return imageMap;
    }

    private Map<Long, List<String>> buildTagMap(List<Long> dishIds) {
        Map<Long, List<String>> tagMap = new LinkedHashMap<>();
        for (LlmDishTagRelationVo relation : recommendationMapper.selectDishTags(dishIds)) {
            if (StringUtils.hasText(relation.getTagName())) {
                tagMap.computeIfAbsent(relation.getDishId(), key -> new ArrayList<>()).add(relation.getTagName());
            }
        }
        return tagMap;
    }

    private List<LlmRecommendationItemVo> toRecommendationItems(
            List<LlmCandidateDishVo> candidates,
            Map<Long, String> reasons
    ) {
        List<LlmRecommendationItemVo> items = new ArrayList<>();
        if (candidates == null || candidates.isEmpty()) {
            return items;
        }
        Map<Long, String> safeReasons = reasons == null ? Collections.emptyMap() : reasons;
        for (LlmCandidateDishVo candidate : candidates) {
            items.add(LlmRecommendationItemVo.builder()
                    .dishId(candidate.getDishId())
                    .name(candidate.getName())
                    .description(candidate.getDescription())
                    .price(candidate.getPrice())
                    .score(candidate.getScore())
                    .categoryId(candidate.getCategoryId())
                    .categoryName(candidate.getCategoryName())
                    .canteenId(candidate.getCanteenId())
                    .canteenName(candidate.getCanteenName())
                    .stallId(candidate.getStallId())
                    .stallName(candidate.getStallName())
                    .merchantId(candidate.getMerchantId())
                    .merchantName(candidate.getMerchantName())
                    .images(candidate.getImages())
                    .tags(candidate.getTags())
                    .softScore(BigDecimal.valueOf(candidate.getSoftScore()).setScale(2, RoundingMode.HALF_UP))
                    .recommendReason(safeReasons.getOrDefault(candidate.getDishId(), candidate.getCurrentRecommendReason()))
                    .build());
        }
        return items;
    }

    private List<String> loadConversationHistory(String conversationId, Long userId, String role) {
        Optional<MongoRecommendationStore> store = getMongoStore();
        if (store.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return store.get()
                    .findConversation(conversationId, userId, role)
                    .map(conversation -> conversation.getMessages().stream()
                            .skip(Math.max(0, conversation.getMessages().size() - 30))
                            .map(message -> message.getRole() + ": " + message.getContent())
                            .toList())
                    .orElse(Collections.emptyList());
        } catch (RuntimeException exception) {
            log.warn("Mongo conversation read failed, recommendation continues without history", exception);
            return Collections.emptyList();
        }
    }

    private void saveConversation(
            String conversationId,
            Long userId,
            String role,
            String userMessage,
            String assistantReply,
            RecommendationReranker.RerankResult rerankResult,
            List<LlmRecommendationItemVo> recommendations
    ) {
        Optional<MongoRecommendationStore> store = getMongoStore();
        if (store.isEmpty()) {
            return;
        }
        try {
            MongoRecommendationStore.ConversationDocument conversation = store.get()
                    .findConversation(conversationId, userId, role)
                    .orElseGet(() -> {
                        MongoRecommendationStore.ConversationDocument document = new MongoRecommendationStore.ConversationDocument();
                        document.setId(conversationId);
                        document.setUserId(userId);
                        document.setRole(role);
                        document.setCreatedAt(LocalDateTime.now());
                        document.setMessages(new ArrayList<>());
                        return document;
                    });
            conversation.getMessages().add(new MongoRecommendationStore.MessageDocument("user", userMessage, LocalDateTime.now()));
            conversation.getMessages().add(new MongoRecommendationStore.MessageDocument("assistant", assistantReply, LocalDateTime.now()));
            if (conversation.getMessages().size() > 30) {
                conversation.setMessages(new ArrayList<>(conversation.getMessages()
                        .subList(conversation.getMessages().size() - 30, conversation.getMessages().size())));
            }
            conversation.setLastRecommendations(recommendations);
            conversation.setLastAction(rerankResult.action());
            conversation.setConversationSummary(rerankResult.conversationSummary());
            conversation.setUpdatedAt(LocalDateTime.now());
            store.get().saveConversation(conversation);
        } catch (RuntimeException exception) {
            log.warn("Mongo conversation save failed, recommendation response has already been built", exception);
        }
    }

    private Optional<MongoRecommendationStore> getMongoStore() {
        if (!properties.isMongoEnabled()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mongoStoreProvider.getIfAvailable());
    }

    private boolean updateProfileMemory(LlmUserProfile profile, RecommendationReranker.RerankResult rerankResult) {
        if (!LocalRecommendationReranker.ACTION_RECOMMEND.equals(rerankResult.action())) {
            return false;
        }
        boolean changed = false;
        if (StringUtils.hasText(rerankResult.longTermMemory())
                && !rerankResult.longTermMemory().equals(profile.getLongTermMemory())) {
            profile.setLongTermMemory(rerankResult.longTermMemory());
            changed = true;
        }
        if (StringUtils.hasText(rerankResult.shortTermMemory())
                && !rerankResult.shortTermMemory().equals(profile.getShortTermMemory())) {
            profile.setShortTermMemory(rerankResult.shortTermMemory());
            changed = true;
        }
        if (changed) {
            profileService.saveProfile(profile);
        }
        return changed;
    }

    private boolean isRecommendationRequested(String message) {
        if (!StringUtils.hasText(message)) {
            return false;
        }
        String normalized = message.trim().toLowerCase();
        return normalized.contains("\u63a8\u8350")
                || normalized.contains("\u5e2e\u6211\u9009")
                || normalized.contains("\u4f60\u51b3\u5b9a")
                || normalized.contains("\u5f00\u59cb\u9009")
                || normalized.contains("\u5c31\u6309")
                || normalized.contains("\u5c31\u8fd9\u4e9b")
                || normalized.contains("\u53ef\u4ee5\u4e86")
                || normalized.contains("\u5403\u4ec0\u4e48")
                || normalized.contains("\u968f\u4fbf")
                || normalized.contains("\u6765\u4e00\u4e2a")
                || normalized.contains("\u9009\u4e00\u4e2a");
    }

    private void validateConstraints(LlmRecommendationConstraints constraints) {
        if (constraints == null) {
            return;
        }
        if (constraints.getMinPrice() != null
                && constraints.getMaxPrice() != null
                && constraints.getMinPrice().compareTo(constraints.getMaxPrice()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 3;
        }
        return Math.min(limit, 3);
    }

    private String normalizeConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return UUID.randomUUID().toString();
        }
        return conversationId.trim();
    }
}
