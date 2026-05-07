package com.eatnow.backend.llmrecommendation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantDailyReviewSampleVo {

    private Long dishId;
    private String dishName;
    private BigDecimal overallScore;
    private BigDecimal tasteScore;
    private BigDecimal portionScore;
    private BigDecimal valueScore;
    private String content;
    private LocalDateTime createdAt;
}
