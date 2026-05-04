package com.eatnow.backend.lottery.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LotteryRecordVo {

    private Long id;
    private Long userId;
    private String drawMode;
    private String sourceType;
    private Long sourceId;
    private String title;
    private BigDecimal price;
    private BigDecimal score;
    private String resultAction;
    private LocalDateTime createdAt;
}
