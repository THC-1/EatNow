package com.eatnow.backend.llmrecommendation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantDailyMetricsVo {

    private Integer todayReviewCount;
    private BigDecimal todayAverageScore;
    private BigDecimal todayTasteScore;
    private BigDecimal todayPortionScore;
    private BigDecimal todayValueScore;
    private Integer todayFeedbackCount;
    private Integer todayNewPendingFeedbackCount;
    private Integer totalPendingFeedbackCount;
    private Integer todayFavoriteCount;
    private Integer todayWantEatCount;
    private Integer todayEatenCount;
    private Integer todayLotteryCount;
    private Integer todayAcceptedLotteryCount;
    private Integer activeRecommendationCount;
    private Integer todayRecommendClickCount;
}
