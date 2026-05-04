package com.eatnow.backend.merchant.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantStatisticsOverviewVo {

    private Integer dishCount;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer reviewCount;
    private BigDecimal averageScore;
    private Integer todayRecommendClickCount;
    private Integer pendingFeedbackCount;
}
