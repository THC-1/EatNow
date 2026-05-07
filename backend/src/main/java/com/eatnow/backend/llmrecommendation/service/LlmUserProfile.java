package com.eatnow.backend.llmrecommendation.service;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmUserProfile {

    private Long userId;
    private String role;
    private Long defaultCanteenId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal averagePositivePrice;
    private Set<String> explicitTasteTags;
    private Set<String> avoidTags;
    private Map<String, Double> tagWeights;
    private Map<Long, Double> categoryWeights;
    private Map<Long, Double> canteenWeights;
    private List<String> favoriteDishNames;
    private List<String> eatenDishNames;
    private List<String> highRatedDishNames;
    private String longTermMemory;
    private String shortTermMemory;
    private String summary;
    private LocalDateTime updatedAt;

    public Map<String, Double> safeTagWeights() {
        return tagWeights == null ? new LinkedHashMap<>() : tagWeights;
    }

    public Map<Long, Double> safeCategoryWeights() {
        return categoryWeights == null ? new LinkedHashMap<>() : categoryWeights;
    }

    public Map<Long, Double> safeCanteenWeights() {
        return canteenWeights == null ? new LinkedHashMap<>() : canteenWeights;
    }
}
