package com.eatnow.backend.llmrecommendation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LlmUserPreferenceSnapshotVo {

    private Long userId;
    private Long defaultCanteenId;
    private String defaultCanteenType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String tastePreference;
    private String avoidTags;
}
