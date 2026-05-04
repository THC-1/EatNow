package com.eatnow.backend.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("lottery_record")
public class LotteryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long ruleId;
    private String drawMode;
    private String sourceType;
    private Long sourceId;
    private String snapshotTitle;
    private BigDecimal snapshotPrice;
    private BigDecimal snapshotScore;
    private String conditionJson;
    private String resultAction;
    private LocalDateTime actionAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
