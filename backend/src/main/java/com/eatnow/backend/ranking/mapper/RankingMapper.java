package com.eatnow.backend.ranking.mapper;

import com.eatnow.backend.ranking.vo.RankingItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingMapper {

    List<RankingItemVo> selectTopRated(@Param("canteenId") Long canteenId, @Param("categoryId") Long categoryId, @Param("limit") int limit);

    List<RankingItemVo> selectPopular(@Param("canteenId") Long canteenId, @Param("limit") int limit);

    List<RankingItemVo> selectMostFavorited(@Param("canteenId") Long canteenId, @Param("limit") int limit);

    List<RankingItemVo> selectBestValue(@Param("canteenId") Long canteenId, @Param("limit") int limit);

    List<RankingItemVo> selectNewDishes(@Param("canteenId") Long canteenId, @Param("limit") int limit);

    List<RankingItemVo> selectMostFeedback(@Param("canteenId") Long canteenId, @Param("limit") int limit);
}
