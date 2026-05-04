package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminPopularMerchantVo {

    private Long merchantId;
    private String merchantName;
    private String canteenName;
    private Integer dishCount;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer reviewCount;
    private BigDecimal averageScore;
}
