package com.eatnow.backend.eatlist.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EatListDishRelationVo {

    private Long dishId;
    private String dishName;
    private BigDecimal price;
    private BigDecimal score;
    private String merchantName;
    private String canteenName;
    private String imageUrl;
}
