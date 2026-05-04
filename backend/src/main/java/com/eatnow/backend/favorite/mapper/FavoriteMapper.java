package com.eatnow.backend.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.favorite.entity.Favorite;
import com.eatnow.backend.favorite.vo.FavoriteItemVo;
import com.eatnow.backend.favorite.vo.FavoriteSummaryRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    long countFavorites(@Param("userId") Long userId, @Param("targetType") String targetType);

    List<FavoriteItemVo> selectFavoriteList(
            @Param("userId") Long userId,
            @Param("targetType") String targetType,
            @Param("offset") long offset,
            @Param("size") int size
    );

    List<FavoriteSummaryRelationVo> selectDishSummaries(@Param("dishIds") List<Long> dishIds);

    List<FavoriteSummaryRelationVo> selectPostSummaries(@Param("postIds") List<Long> postIds);

    Integer countDishById(@Param("id") Long id);

    Integer countPostById(@Param("id") Long id);

    int increaseDishFavoriteCount(@Param("id") Long id);

    int decreaseDishFavoriteCount(@Param("id") Long id);

    int increasePostFavoriteCount(@Param("id") Long id);

    int decreasePostFavoriteCount(@Param("id") Long id);
}
