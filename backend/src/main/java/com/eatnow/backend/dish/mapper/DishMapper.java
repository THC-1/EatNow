package com.eatnow.backend.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.dish.dto.DishQueryRequest;
import com.eatnow.backend.dish.entity.Dish;
import com.eatnow.backend.dish.vo.DishDetailVo;
import com.eatnow.backend.dish.vo.DishImageRelationVo;
import com.eatnow.backend.dish.vo.DishListVo;
import com.eatnow.backend.dish.vo.DishTagRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    long countDishes(@Param("query") DishQueryRequest query);

    List<DishListVo> selectDishList(
            @Param("query") DishQueryRequest query,
            @Param("offset") long offset,
            @Param("size") int size
    );

    DishDetailVo selectDishDetail(@Param("id") Long id, @Param("statusList") List<String> statusList);

    DishDetailVo selectDishManageDetail(@Param("id") Long id, @Param("merchantId") Long merchantId);

    List<DishTagRelationVo> selectDishTagsByDishIds(@Param("dishIds") List<Long> dishIds);

    List<DishImageRelationVo> selectDishImagesByDishIds(@Param("dishIds") List<Long> dishIds);

    String selectCurrentRecommendReason(@Param("dishId") Long dishId);

    int increaseViewCount(@Param("id") Long id);

    int deleteDishImages(@Param("dishId") Long dishId);

    int insertDishImages(@Param("dishId") Long dishId, @Param("images") List<String> images);

    int deleteDishTags(@Param("dishId") Long dishId);

    int insertDishTags(@Param("dishId") Long dishId, @Param("tagIds") List<Long> tagIds);

    int upsertDishLotteryPool(@Param("dishId") Long dishId);

    int updateDishLotteryPoolStatus(@Param("dishId") Long dishId, @Param("status") String status);
}
