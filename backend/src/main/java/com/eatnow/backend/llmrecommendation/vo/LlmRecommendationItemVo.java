package com.eatnow.backend.llmrecommendation.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class LlmRecommendationItemVo {

    private Long dishId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal score;
    private Long categoryId;
    private String categoryName;
    private Long canteenId;
    private String canteenName;
    private Long stallId;
    private String stallName;
    private Long merchantId;
    private String merchantName;
    private List<String> images;
    private List<String> tags;
    private BigDecimal softScore;
    private String recommendReason;
}
