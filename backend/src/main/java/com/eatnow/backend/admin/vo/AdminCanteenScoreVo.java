package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminCanteenScoreVo {

    private Long canteenId;
    private String canteenName;
    private String canteenType;
    private BigDecimal averageScore;
    private Integer reviewCount;
    private Integer dishCount;
}
