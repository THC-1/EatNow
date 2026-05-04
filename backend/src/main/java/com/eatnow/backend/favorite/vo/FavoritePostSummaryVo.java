package com.eatnow.backend.favorite.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FavoritePostSummaryVo {

    private Long id;
    private String title;
    private String dishName;
    private BigDecimal price;
    private BigDecimal score;
    private List<String> images;
}
