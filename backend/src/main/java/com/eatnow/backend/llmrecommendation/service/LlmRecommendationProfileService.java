package com.eatnow.backend.llmrecommendation.service;

import com.eatnow.backend.llmrecommendation.config.LlmRecommendationProperties;
import com.eatnow.backend.llmrecommendation.mapper.LlmRecommendationMapper;
import com.eatnow.backend.llmrecommendation.mongo.MongoRecommendationStore;
import com.eatnow.backend.llmrecommendation.vo.LlmSignalWeightVo;
import com.eatnow.backend.llmrecommendation.vo.LlmUserPreferenceSnapshotVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmRecommendationProfileService {

    private final LlmRecommendationMapper recommendationMapper;
    private final LlmRecommendationProperties properties;
    private final ObjectProvider<MongoRecommendationStore> mongoStoreProvider;

    public LlmUserProfile getOrRefreshProfile(Long userId, String role) {
        Optional<MongoRecommendationStore> store = getMongoStore();
        LlmUserProfile cached = null;
        if (store.isPresent()) {
            try {
                cached = store.get().findUserProfile(userId, role).orElse(null);
                if (cached != null && cached.getUpdatedAt() != null
                        && cached.getUpdatedAt().isAfter(LocalDateTime.now().minusMinutes(properties.getProfileTtlMinutes()))) {
                    return cached;
                }
            } catch (RuntimeException exception) {
                log.warn("Mongo user profile read failed, fallback to MySQL profile aggregation", exception);
            }
        }

        LlmUserProfile profile = buildProfileFromMysql(userId, role);
        if (cached != null) {
            profile.setLongTermMemory(cached.getLongTermMemory());
            profile.setShortTermMemory(cached.getShortTermMemory());
        }
        saveProfile(profile);
        return profile;
    }

    public void saveProfile(LlmUserProfile profile) {
        Optional<MongoRecommendationStore> store = getMongoStore();
        if (store.isEmpty()) {
            return;
        }
        try {
            profile.setUpdatedAt(LocalDateTime.now());
            store.get().saveUserProfile(profile);
        } catch (RuntimeException exception) {
            log.warn("Mongo user profile save failed, recommendation continues locally", exception);
        }
    }

    private LlmUserProfile buildProfileFromMysql(Long userId, String role) {
        LlmUserPreferenceSnapshotVo preference = recommendationMapper.selectUserPreferenceSnapshot(userId);
        Set<String> tasteTags = splitTags(preference == null ? null : preference.getTastePreference());
        Set<String> avoidTags = splitTags(preference == null ? null : preference.getAvoidTags());
        Map<String, Double> tagWeights = toNameWeightMap(recommendationMapper.selectUserTagSignals(userId));
        for (String tasteTag : tasteTags) {
            tagWeights.merge(tasteTag, 3.0, Double::sum);
        }

        Map<Long, Double> categoryWeights = toIdWeightMap(recommendationMapper.selectUserCategorySignals(userId));
        Map<Long, Double> canteenWeights = toIdWeightMap(recommendationMapper.selectUserCanteenSignals(userId));
        BigDecimal averagePositivePrice = recommendationMapper.selectUserAveragePositivePrice(userId);
        List<String> favoriteDishNames = safeList(recommendationMapper.selectUserFavoriteDishNames(userId));
        List<String> eatenDishNames = safeList(recommendationMapper.selectUserEatenDishNames(userId));
        List<String> highRatedDishNames = safeList(recommendationMapper.selectUserHighRatedDishNames(userId));

        return LlmUserProfile.builder()
                .userId(userId)
                .role(role)
                .defaultCanteenId(preference == null ? null : preference.getDefaultCanteenId())
                .minPrice(preference == null ? null : preference.getMinPrice())
                .maxPrice(preference == null ? null : preference.getMaxPrice())
                .averagePositivePrice(averagePositivePrice)
                .explicitTasteTags(tasteTags)
                .avoidTags(avoidTags)
                .tagWeights(tagWeights)
                .categoryWeights(categoryWeights)
                .canteenWeights(canteenWeights)
                .favoriteDishNames(favoriteDishNames)
                .eatenDishNames(eatenDishNames)
                .highRatedDishNames(highRatedDishNames)
                .summary(buildSummary(tasteTags, avoidTags, tagWeights, preference,
                        favoriteDishNames, eatenDishNames, highRatedDishNames))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Optional<MongoRecommendationStore> getMongoStore() {
        if (!properties.isMongoEnabled()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mongoStoreProvider.getIfAvailable());
    }

    private Set<String> splitTags(String value) {
        Set<String> tags = new LinkedHashSet<>();
        if (!StringUtils.hasText(value)) {
            return tags;
        }
        Arrays.stream(value.split("[,;\\s，；、]+"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(tags::add);
        return tags;
    }

    private Map<String, Double> toNameWeightMap(Iterable<LlmSignalWeightVo> signals) {
        Map<String, Double> weights = new LinkedHashMap<>();
        for (LlmSignalWeightVo signal : signals) {
            if (StringUtils.hasText(signal.getName())) {
                weights.put(signal.getName(), signal.getWeight() == null ? 0.0 : signal.getWeight());
            }
        }
        return weights;
    }

    private Map<Long, Double> toIdWeightMap(Iterable<LlmSignalWeightVo> signals) {
        Map<Long, Double> weights = new LinkedHashMap<>();
        for (LlmSignalWeightVo signal : signals) {
            if (signal.getId() != null) {
                weights.put(signal.getId(), signal.getWeight() == null ? 0.0 : signal.getWeight());
            }
        }
        return weights;
    }

    private String buildSummary(
            Set<String> tasteTags,
            Set<String> avoidTags,
            Map<String, Double> tagWeights,
            LlmUserPreferenceSnapshotVo preference,
            List<String> favoriteDishNames,
            List<String> eatenDishNames,
            List<String> highRatedDishNames
    ) {
        StringBuilder builder = new StringBuilder();
        if (!tasteTags.isEmpty()) {
            builder.append("Taste preferences: ").append(String.join(", ", tasteTags)).append(". ");
        } else if (!tagWeights.isEmpty()) {
            builder.append("Frequent tags: ").append(String.join(", ", tagWeights.keySet().stream().limit(5).toList())).append(". ");
        }
        if (!avoidTags.isEmpty()) {
            builder.append("Avoids: ").append(String.join(", ", avoidTags)).append(". ");
        }
        if (preference != null && (preference.getMinPrice() != null || preference.getMaxPrice() != null)) {
            builder.append("Budget: ")
                    .append(preference.getMinPrice() == null ? "unlimited" : preference.getMinPrice())
                    .append("-")
                    .append(preference.getMaxPrice() == null ? "unlimited" : preference.getMaxPrice())
                    .append(". ");
        }
        appendNames(builder, "Favorite dishes", favoriteDishNames);
        appendNames(builder, "Eaten dishes", eatenDishNames);
        appendNames(builder, "High-rated dishes", highRatedDishNames);
        return builder.isEmpty()
                ? "No stable profile yet; prefer public scores, popularity, and current request."
                : builder.toString();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values.stream().filter(StringUtils::hasText).distinct().limit(20).toList();
    }

    private void appendNames(StringBuilder builder, String label, List<String> names) {
        if (names == null || names.isEmpty()) {
            return;
        }
        builder.append(label).append(": ").append(String.join(", ", names.stream().limit(5).toList())).append(". ");
    }
}
