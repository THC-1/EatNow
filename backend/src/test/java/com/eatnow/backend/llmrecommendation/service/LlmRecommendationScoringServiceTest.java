package com.eatnow.backend.llmrecommendation.service;

import com.eatnow.backend.llmrecommendation.dto.LlmRecommendationConstraints;
import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LlmRecommendationScoringServiceTest {

    private final LlmRecommendationScoringService scoringService = new LlmRecommendationScoringService();

    @Test
    void preferenceAndBudgetSignalsRaiseScore() {
        LlmCandidateDishVo matched = dish(1L, "香辣鸡饭", BigDecimal.valueOf(18), List.of("香辣", "鸡肉"));
        matched.setCategoryId(10L);
        matched.setCanteenId(20L);
        matched.setFavoriteCount(10);
        matched.setViewCount(100);
        matched.setCommentCount(8);

        LlmCandidateDishVo plain = dish(2L, "清汤面", BigDecimal.valueOf(28), List.of("清淡"));
        plain.setCategoryId(11L);
        plain.setCanteenId(21L);
        plain.setFavoriteCount(1);
        plain.setViewCount(20);
        plain.setCommentCount(1);

        LlmUserProfile profile = profile(Set.of("香辣"), Set.of("肥腻"));
        profile.setDefaultCanteenId(20L);
        profile.setMinPrice(BigDecimal.valueOf(10));
        profile.setMaxPrice(BigDecimal.valueOf(20));
        profile.setCategoryWeights(Map.of(10L, 8.0));
        profile.setCanteenWeights(Map.of(20L, 6.0));

        scoringService.score(List.of(matched, plain), profile, "想吃香辣一点的", new LlmRecommendationConstraints());

        assertThat(matched.getSoftScore()).isGreaterThan(plain.getSoftScore());
    }

    @Test
    void avoidTagsLowerScore() {
        LlmCandidateDishVo avoided = dish(1L, "重油拌饭", BigDecimal.valueOf(15), List.of("重油"));
        LlmCandidateDishVo normal = dish(2L, "番茄牛肉饭", BigDecimal.valueOf(15), List.of("番茄"));
        LlmUserProfile profile = profile(Set.of(), Set.of("重油"));

        scoringService.score(List.of(avoided, normal), profile, "随便推荐", null);

        assertThat(avoided.getSoftScore()).isLessThan(normal.getSoftScore());
    }

    private LlmCandidateDishVo dish(Long id, String name, BigDecimal price, List<String> tags) {
        LlmCandidateDishVo dish = new LlmCandidateDishVo();
        dish.setDishId(id);
        dish.setName(name);
        dish.setPrice(price);
        dish.setScore(BigDecimal.valueOf(4.5));
        dish.setTasteScore(BigDecimal.valueOf(4.5));
        dish.setPortionScore(BigDecimal.valueOf(4.3));
        dish.setValueScore(BigDecimal.valueOf(4.2));
        dish.setTags(tags);
        return dish;
    }

    private LlmUserProfile profile(Set<String> tasteTags, Set<String> avoidTags) {
        return LlmUserProfile.builder()
                .userId(1L)
                .role("STUDENT")
                .explicitTasteTags(new LinkedHashSet<>(tasteTags))
                .avoidTags(new LinkedHashSet<>(avoidTags))
                .tagWeights(new LinkedHashMap<>())
                .categoryWeights(new LinkedHashMap<>())
                .canteenWeights(new LinkedHashMap<>())
                .build();
    }
}
