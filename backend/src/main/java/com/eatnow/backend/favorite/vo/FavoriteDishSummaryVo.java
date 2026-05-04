package com.eatnow.backend.favorite.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FavoriteDishSummaryVo {

    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal score;
    private List<String> images;
}
