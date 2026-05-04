package com.eatnow.backend.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.lottery.entity.LotteryRecord;
import com.eatnow.backend.lottery.vo.LotteryRecordVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LotteryRecordMapper extends BaseMapper<LotteryRecord> {

    long countByUserId(@Param("userId") Long userId);

    List<LotteryRecordVo> selectByUserId(
            @Param("userId") Long userId,
            @Param("offset") long offset,
            @Param("size") int size
    );
}
