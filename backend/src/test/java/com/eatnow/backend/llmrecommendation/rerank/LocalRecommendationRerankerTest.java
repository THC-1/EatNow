package com.eatnow.backend.llmrecommendation.rerank;

import com.eatnow.backend.llmrecommendation.service.LlmUserProfile;
import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalRecommendationRerankerTest {

    private final LocalRecommendationReranker reranker = new LocalRecommendationReranker();

    @Test
    void ranksBySoftScoreAndMarksFallback() {
        LlmCandidateDishVo lower = dish(1L, "low score dish", 70);
        LlmCandidateDishVo higher = dish(2L, "high score dish", 90);

        RecommendationReranker.RerankResult result = reranker.rerank(new RecommendationReranker.RerankRequest(
                "recommend dinner",
                LlmUserProfile.builder().userId(1L).role("STUDENT").build(),
                Collections.emptyList(),
                List.of(lower, higher),
                1,
                true
        ));

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.action()).isEqualTo(LocalRecommendationReranker.ACTION_RECOMMEND);
        assertThat(result.rankedCandidates()).extracting(LlmCandidateDishVo::getDishId).containsExactly(2L);
        assertThat(result.reasons()).containsKey(2L);
    }

    @Test
    void asksWhenRecommendationIsNotRequested() {
        RecommendationReranker.RerankResult result = reranker.rerank(new RecommendationReranker.RerankRequest(
                "light food",
                LlmUserProfile.builder().userId(1L).role("STUDENT").build(),
                Collections.emptyList(),
                List.of(dish(1L, "soup noodle", 80)),
                1,
                false
        ));

        assertThat(result.action()).isEqualTo(LocalRecommendationReranker.ACTION_ASK);
        assertThat(result.rankedCandidates()).isEmpty();
    }

    private LlmCandidateDishVo dish(Long id, String name, double score) {
        LlmCandidateDishVo dish = new LlmCandidateDishVo();
        dish.setDishId(id);
        dish.setName(name);
        dish.setSoftScore(score);
        return dish;
    }
}
