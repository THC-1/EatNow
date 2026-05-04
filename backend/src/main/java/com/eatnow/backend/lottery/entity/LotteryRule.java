package com.eatnow.backend.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("lottery_rule")
public class LotteryRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleName;
    private String drawMode;
    private BigDecimal studentRatio;
    private BigDecimal merchantRatio;
    private BigDecimal minScore;
    private BigDecimal maxPrice;
    private Boolean isDefault;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
