package com.eatnow.backend.ranking.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RankingItemVo {

    private Integer rank;
    private Long dishId;
    private String dishName;
    private String merchantName;
    private String canteenName;
    private String canteenType;
    private BigDecimal score;
    private BigDecimal price;
    private Integer favoriteCount;
    private Integer viewCount;
    private Integer reviewCount;
    private Integer feedbackCount;
    private String coverImageUrl;
    private List<String> images;
}
