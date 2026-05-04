package com.eatnow.backend.merchant.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantDetailVo {

    private Long id;
    private Long userId;
    private Long stallId;
    private String name;
    private String description;
    private String logoUrl;
    private String businessHours;
    private String status;
    private String canteenName;
    private String canteenType;
    private String stallName;
    private BigDecimal averageScore;
    private Long dishCount;
    private Integer favoriteCount;
    private Integer commentCount;
}
