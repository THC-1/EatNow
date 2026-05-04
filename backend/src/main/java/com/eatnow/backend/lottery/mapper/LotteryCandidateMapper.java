package com.eatnow.backend.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.lottery.entity.LotteryCandidate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LotteryCandidateMapper extends BaseMapper<LotteryCandidate> {
}
