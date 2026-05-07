package com.eatnow.backend.llmrecommendation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class LlmCandidateDishVo {

    private Long dishId;
    private Long merchantId;
    private String merchantName;
    private Long canteenId;
    private String canteenName;
    private Long stallId;
    private String stallName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal score;
    private BigDecimal tasteScore;
    private BigDecimal portionScore;
    private BigDecimal valueScore;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private String coverImageUrl;
    private String currentRecommendReason;
    private String currentRecommendType;
    private LocalDateTime createdAt;
    private List<String> images = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private double softScore;
}
