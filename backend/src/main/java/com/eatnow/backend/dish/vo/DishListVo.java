package com.eatnow.backend.dish.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DishListVo {

    private Long id;
    private Long merchantId;
    private Long canteenId;
    private Long stallId;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private BigDecimal score;
    private String status;
    private List<String> images;
    private List<DishTagVo> tags;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer commentCount;
}
