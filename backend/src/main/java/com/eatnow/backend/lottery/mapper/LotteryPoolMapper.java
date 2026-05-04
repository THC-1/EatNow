package com.eatnow.backend.lottery.mapper;

import com.eatnow.backend.lottery.vo.LotteryImageRelationVo;
import com.eatnow.backend.lottery.vo.LotteryPoolItemVo;
import com.eatnow.backend.lottery.vo.LotteryTagRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LotteryPoolMapper {

    List<LotteryPoolItemVo> selectEligiblePools(
            @Param("canteenId") Long canteenId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minScore") BigDecimal minScore,
            @Param("tagIds") List<Long> tagIds,
            @Param("favoriteUserId") Long favoriteUserId
    );

    List<LotteryTagRelationVo> selectDishTags(@Param("dishIds") List<Long> dishIds);

    List<LotteryImageRelationVo> selectDishImages(@Param("dishIds") List<Long> dishIds);
}
