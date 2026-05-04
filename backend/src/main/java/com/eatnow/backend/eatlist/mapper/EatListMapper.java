package com.eatnow.backend.eatlist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.eatlist.entity.EatList;
import com.eatnow.backend.eatlist.vo.EatListDishRelationVo;
import com.eatnow.backend.eatlist.vo.EatListItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EatListMapper extends BaseMapper<EatList> {

    long countEatList(@Param("userId") Long userId, @Param("status") String status);

    List<EatListItemVo> selectEatList(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("offset") long offset,
            @Param("size") int size
    );

    List<EatListDishRelationVo> selectDishRelations(@Param("dishIds") List<Long> dishIds);

    Integer countVisibleDishById(@Param("id") Long id);
}
