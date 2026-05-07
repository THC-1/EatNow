package com.eatnow.backend.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyDishMetricVo;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyFeedbackSampleVo;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyMetricsVo;
import com.eatnow.backend.llmrecommendation.vo.MerchantDailyReviewSampleVo;
import com.eatnow.backend.merchant.entity.Merchant;
import com.eatnow.backend.merchant.vo.MerchantDetailVo;
import com.eatnow.backend.merchant.vo.MerchantFeedbackDishVo;
import com.eatnow.backend.merchant.vo.MerchantListVo;
import com.eatnow.backend.merchant.vo.MerchantPopularDishVo;
import com.eatnow.backend.merchant.vo.MerchantProfileVo;
import com.eatnow.backend.merchant.vo.MerchantStatisticsOverviewVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    List<MerchantListVo> selectMerchantList(
            @Param("canteenId") Long canteenId,
            @Param("stallId") Long stallId,
            @Param("status") String status
    );

    MerchantDetailVo selectMerchantDetail(@Param("id") Long id, @Param("status") String status);

    MerchantProfileVo selectMerchantProfileByUserId(@Param("userId") Long userId);

    MerchantStatisticsOverviewVo selectMerchantStatisticsOverview(@Param("merchantId") Long merchantId);

    List<MerchantPopularDishVo> selectMerchantPopularDishes(
            @Param("merchantId") Long merchantId,
            @Param("limit") int limit
    );

    List<MerchantFeedbackDishVo> selectMerchantMostFeedbackDishes(
            @Param("merchantId") Long merchantId,
            @Param("limit") int limit
    );

    MerchantDailyMetricsVo selectMerchantDailyMetrics(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<MerchantDailyDishMetricVo> selectMerchantDailyDishMetrics(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    List<MerchantDailyReviewSampleVo> selectMerchantDailyReviewSamples(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );

    List<MerchantDailyFeedbackSampleVo> selectMerchantDailyFeedbackSamples(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );
}
