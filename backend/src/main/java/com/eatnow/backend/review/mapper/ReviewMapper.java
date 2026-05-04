package com.eatnow.backend.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.review.entity.Review;
import com.eatnow.backend.review.vo.ReviewAggregateVo;
import com.eatnow.backend.review.vo.ReviewImageRelationVo;
import com.eatnow.backend.review.vo.ReviewItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    Integer countVisibleDishById(@Param("id") Long id);

    Long selectMerchantIdByDishId(@Param("dishId") Long dishId);

    long countReviewList(
            @Param("dishId") Long dishId,
            @Param("status") String status,
            @Param("userId") Long userId
    );

    List<ReviewItemVo> selectReviewList(
            @Param("dishId") Long dishId,
            @Param("status") String status,
            @Param("sortBy") String sortBy,
            @Param("offset") long offset,
            @Param("size") int size
    );

    List<ReviewItemVo> selectMyReviewList(
            @Param("userId") Long userId,
            @Param("offset") long offset,
            @Param("size") int size
    );

    long countMyReviewList(@Param("userId") Long userId);

    List<ReviewImageRelationVo> selectReviewImages(@Param("reviewIds") List<Long> reviewIds);

    List<Long> selectLikedReviewIds(@Param("reviewIds") List<Long> reviewIds, @Param("userId") Long userId);

    ReviewAggregateVo selectDishReviewAggregate(@Param("dishId") Long dishId);

    ReviewAggregateVo selectMerchantReviewAggregate(@Param("merchantId") Long merchantId);

    int increaseReviewLikeCount(@Param("id") Long id);

    int decreaseReviewLikeCount(@Param("id") Long id);

    int updateDishReviewStats(
            @Param("dishId") Long dishId,
            @Param("reviewCount") Integer reviewCount,
            @Param("averageScore") java.math.BigDecimal averageScore,
            @Param("tasteScore") java.math.BigDecimal tasteScore,
            @Param("portionScore") java.math.BigDecimal portionScore,
            @Param("valueScore") java.math.BigDecimal valueScore
    );

    int updateMerchantReviewStats(
            @Param("merchantId") Long merchantId,
            @Param("reviewCount") Integer reviewCount,
            @Param("averageScore") java.math.BigDecimal averageScore
    );
}
