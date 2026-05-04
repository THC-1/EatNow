package com.eatnow.backend.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.campus.entity.Canteen;
import com.eatnow.backend.campus.vo.CanteenDetailVo;
import com.eatnow.backend.campus.vo.CanteenListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CanteenMapper extends BaseMapper<Canteen> {

    List<CanteenListVo> selectCanteenList(
            @Param("campusId") Long campusId,
            @Param("type") String type,
            @Param("status") String status
    );

    CanteenDetailVo selectCanteenDetail(@Param("id") Long id, @Param("status") String status);
}
