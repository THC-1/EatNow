package com.eatnow.backend.llmrecommendation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantDailyDishMetricVo {

    private Long dishId;
    private String dishName;
    private String status;
    private BigDecimal price;
    private BigDecimal averageScore;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer reviewCount;
    private Integer todayReviewCount;
    private Integer todayFeedbackCount;
    private Integer todayFavoriteCount;
    private Integer todayWantEatCount;
    private Integer todayLotteryCount;
}
