package com.eatnow.backend.favorite.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FavoriteSummaryRelationVo {

    private Long id;
    private String title;
    private String dishName;
    private BigDecimal price;
    private BigDecimal score;
    private String imageUrl;
}
