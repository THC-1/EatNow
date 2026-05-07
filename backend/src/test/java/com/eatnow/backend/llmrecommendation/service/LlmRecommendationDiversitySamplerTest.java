package com.eatnow.backend.llmrecommendation.service;

import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LlmRecommendationDiversitySamplerTest {

    private final LlmRecommendationDiversitySampler sampler = new LlmRecommendationDiversitySampler();

    @Test
    void samplesAcrossMerchantCategoryTagAndPriceThenUsesPopularFillers() {
        List<LlmCandidateDishVo> narrow = List.of(
                dish(1L, 10L, 100L, BigDecimal.valueOf(12), List.of("辣"), 92),
                dish(2L, 10L, 100L, BigDecimal.valueOf(13), List.of("辣"), 91)
        );
        List<LlmCandidateDishVo> fillers = List.of(
                dish(1L, 10L, 100L, BigDecimal.valueOf(12), List.of("辣"), 92),
                dish(3L, 11L, 101L, BigDecimal.valueOf(22), List.of("清淡"), 80),
                dish(4L, 12L, 102L, BigDecimal.valueOf(38), List.of("甜口"), 70)
        );

        List<LlmCandidateDishVo> sampled = sampler.sample(narrow, fillers, 4);

        assertThat(sampled).extracting(LlmCandidateDishVo::getDishId).containsExactly(1L, 2L, 3L, 4L);
        assertThat(sampled).extracting(LlmCandidateDishVo::getDishId).doesNotHaveDuplicates();
        assertThat(sampled).extracting(LlmCandidateDishVo::getMerchantId).contains(10L, 11L, 12L);
        assertThat(sampled).extracting(LlmCandidateDishVo::getCategoryId).contains(100L, 101L, 102L);
    }

    private LlmCandidateDishVo dish(
            Long dishId,
            Long merchantId,
            Long categoryId,
            BigDecimal price,
            List<String> tags,
            double score
    ) {
        LlmCandidateDishVo dish = new LlmCandidateDishVo();
        dish.setDishId(dishId);
        dish.setMerchantId(merchantId);
        dish.setCategoryId(categoryId);
        dish.setPrice(price);
        dish.setTags(tags);
        dish.setSoftScore(score);
        return dish;
    }
}
