package com.eatnow.backend.lottery.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LotteryDrawResultVo {

    private Long recordId;
    private String sourceType;
    private Long sourceId;
    private String title;
    private String canteenName;
    private String canteenType;
    private String merchantName;
    private BigDecimal price;
    private BigDecimal score;
    private List<String> tags;
    private List<String> images;
    private String recommendReason;
}
