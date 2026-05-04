package com.eatnow.backend.review.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewAggregateVo {

    private Integer reviewCount;
    private BigDecimal averageScore;
    private BigDecimal tasteScore;
    private BigDecimal portionScore;
    private BigDecimal valueScore;
}
