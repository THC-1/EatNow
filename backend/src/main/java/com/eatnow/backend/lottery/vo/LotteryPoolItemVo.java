package com.eatnow.backend.lottery.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LotteryPoolItemVo {

    private Long poolId;
    private String sourceType;
    private Long sourceId;
    private Long merchantId;
    private Long canteenId;
    private Long stallId;
    private String title;
    private BigDecimal price;
    private BigDecimal score;
    private String coverImageUrl;
    private String recommendReason;
    private Integer weight;
    private String merchantName;
    private String canteenName;
    private String canteenType;
    private String stallName;
}
