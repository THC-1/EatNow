package com.eatnow.backend.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.campus.entity.Stall;
import com.eatnow.backend.campus.vo.StallDetailVo;
import com.eatnow.backend.campus.vo.StallListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StallMapper extends BaseMapper<Stall> {

    List<StallListVo> selectStallList(@Param("canteenId") Long canteenId, @Param("status") String status);

    StallDetailVo selectStallDetail(@Param("id") Long id, @Param("status") String status);
}
