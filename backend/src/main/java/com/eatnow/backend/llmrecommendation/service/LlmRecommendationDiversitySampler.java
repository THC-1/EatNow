package com.eatnow.backend.llmrecommendation.service;

import com.eatnow.backend.llmrecommendation.vo.LlmCandidateDishVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LlmRecommendationDiversitySampler {

    public List<LlmCandidateDishVo> sample(
            List<LlmCandidateDishVo> scoredCandidates,
            List<LlmCandidateDishVo> popularFillers,
            int sampleSize
    ) {
        List<LlmCandidateDishVo> sorted = new ArrayList<>(scoredCandidates);
        sorted.sort(scoreComparator());

        List<LlmCandidateDishVo> selected = new ArrayList<>();
        Set<Long> usedDishIds = new LinkedHashSet<>();
        Map<Long, Integer> merchantCounts = new HashMap<>();
        Set<Long> usedCategories = new HashSet<>();
        Set<String> usedPriceBuckets = new HashSet<>();
        Set<String> usedTagNames = new HashSet<>();
        int merchantCap = Math.max(2, sampleSize / 3);

        pickPass(sorted, selected, usedDishIds, merchantCounts, usedCategories, usedPriceBuckets, usedTagNames, sampleSize, merchantCap, true);
        pickPass(sorted, selected, usedDishIds, merchantCounts, usedCategories, usedPriceBuckets, usedTagNames, sampleSize, merchantCap, false);

        List<LlmCandidateDishVo> fillers = new ArrayList<>(popularFillers);
        fillers.sort(scoreComparator());
        pickPass(fillers, selected, usedDishIds, merchantCounts, usedCategories, usedPriceBuckets, usedTagNames, sampleSize, merchantCap + 1, false);
        return selected;
    }

    private void pickPass(
            List<LlmCandidateDishVo> candidates,
            List<LlmCandidateDishVo> selected,
            Set<Long> usedDishIds,
            Map<Long, Integer> merchantCounts,
            Set<Long> usedCategories,
            Set<String> usedPriceBuckets,
            Set<String> usedTagNames,
            int sampleSize,
            int merchantCap,
            boolean strictDishDiversity
    ) {
        for (LlmCandidateDishVo candidate : candidates) {
            if (selected.size() >= sampleSize) {
                return;
            }
            if (candidate.getDishId() == null || usedDishIds.contains(candidate.getDishId())) {
                continue;
            }
            int merchantCount = merchantCounts.getOrDefault(candidate.getMerchantId(), 0);
            if (merchantCount >= merchantCap) {
                continue;
            }
            if (strictDishDiversity && isTooSimilar(candidate, usedCategories, usedPriceBuckets, usedTagNames)) {
                continue;
            }
            selected.add(candidate);
            usedDishIds.add(candidate.getDishId());
            merchantCounts.merge(candidate.getMerchantId(), 1, Integer::sum);
            if (candidate.getCategoryId() != null) {
                usedCategories.add(candidate.getCategoryId());
            }
            usedPriceBuckets.add(priceBucket(candidate.getPrice()));
            usedTagNames.addAll(candidate.getTags());
        }
    }

    private boolean isTooSimilar(
            LlmCandidateDishVo candidate,
            Set<Long> usedCategories,
            Set<String> usedPriceBuckets,
            Set<String> usedTagNames
    ) {
        boolean categoryUsed = candidate.getCategoryId() != null && usedCategories.contains(candidate.getCategoryId());
        boolean priceUsed = usedPriceBuckets.contains(priceBucket(candidate.getPrice()));
        boolean tagOverlaps = candidate.getTags().stream().anyMatch(usedTagNames::contains);
        return categoryUsed && priceUsed && tagOverlaps;
    }

    private Comparator<LlmCandidateDishVo> scoreComparator() {
        return Comparator.comparingDouble(LlmCandidateDishVo::getSoftScore).reversed()
                .thenComparing(candidate -> candidate.getScore() == null ? BigDecimal.ZERO : candidate.getScore(), Comparator.reverseOrder())
                .thenComparing(LlmCandidateDishVo::getDishId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private String priceBucket(BigDecimal price) {
        if (price == null) {
            return "unknown";
        }
        double value = price.doubleValue();
        if (value < 10) {
            return "lt10";
        }
        if (value < 20) {
            return "10to20";
        }
        if (value < 35) {
            return "20to35";
        }
        return "gte35";
    }
}
