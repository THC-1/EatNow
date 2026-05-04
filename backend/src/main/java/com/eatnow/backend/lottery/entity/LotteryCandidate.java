package com.eatnow.backend.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("lottery_candidate")
public class LotteryCandidate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;
    private String sourceType;
    private Long sourceId;
    private String title;
    private BigDecimal price;
    private BigDecimal score;
    private Integer sortOrder;
    private Boolean isSelected;
    private LocalDateTime createdAt;
}
