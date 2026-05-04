package com.eatnow.backend.merchantrecommendation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.merchantrecommendation.entity.MerchantRecommendation;
import com.eatnow.backend.merchantrecommendation.vo.DishRecommendationVo;
import com.eatnow.backend.merchantrecommendation.vo.MerchantRecommendationItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantRecommendationMapper extends BaseMapper<MerchantRecommendation> {

    long countMerchantRecommendations(
            @Param("merchantId") Long merchantId,
            @Param("recommendType") String recommendType,
            @Param("status") String status
    );

    List<MerchantRecommendationItemVo> selectMerchantRecommendations(
            @Param("merchantId") Long merchantId,
            @Param("recommendType") String recommendType,
            @Param("status") String status,
            @Param("offset") long offset,
            @Param("size") int size
    );

    List<DishRecommendationVo> selectPublicDishRecommendations(
            @Param("merchantId") Long merchantId,
            @Param("recommendType") String recommendType
    );
}
