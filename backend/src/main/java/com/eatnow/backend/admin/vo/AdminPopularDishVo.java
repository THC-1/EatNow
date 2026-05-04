package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminPopularDishVo {

    private Long dishId;
    private String dishName;
    private String merchantName;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer reviewCount;
    private BigDecimal averageScore;
}
