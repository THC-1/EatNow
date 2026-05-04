package com.eatnow.backend.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eatnow.backend.lottery.entity.LotteryRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LotteryRuleMapper extends BaseMapper<LotteryRule> {

    @Select("""
            SELECT *
            FROM lottery_rule
            WHERE draw_mode = #{drawMode}
              AND status = 'ACTIVE'
            ORDER BY is_default DESC, id ASC
            LIMIT 1
            """)
    LotteryRule selectActiveRule(@Param("drawMode") String drawMode);
}
