package com.eatnow.backend.llmrecommendation.vo;

import com.eatnow.backend.merchant.vo.MerchantFeedbackDishVo;
import com.eatnow.backend.merchant.vo.MerchantPopularDishVo;
import com.eatnow.backend.merchant.vo.MerchantStatisticsOverviewVo;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MerchantDailyAnalysisSnapshot {

    private Long merchantId;
    private Long userId;
    private String merchantName;
    private LocalDate reportDate;
    private LocalDateTime generatedAt;
    private MerchantStatisticsOverviewVo overview;
    private MerchantDailyMetricsVo dailyMetrics;
    private List<MerchantPopularDishVo> popularDishes = new ArrayList<>();
    private List<MerchantFeedbackDishVo> feedbackDishes = new ArrayList<>();
    private List<MerchantDailyDishMetricVo> dailyDishMetrics = new ArrayList<>();
    private List<MerchantDailyReviewSampleVo> reviewSamples = new ArrayList<>();
    private List<MerchantDailyFeedbackSampleVo> feedbackSamples = new ArrayList<>();
}
