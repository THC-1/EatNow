package com.eatnow.backend.merchant.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantPopularDishVo {

    private Long dishId;
    private String dishName;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer reviewCount;
    private BigDecimal averageScore;
}
