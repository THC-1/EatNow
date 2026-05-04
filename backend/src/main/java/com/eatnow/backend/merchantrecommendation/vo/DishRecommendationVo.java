package com.eatnow.backend.merchantrecommendation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DishRecommendationVo {

    private Long id;
    private Long merchantId;
    private String merchantName;
    private Long dishId;
    private String dishName;
    private String title;
    private String recommendReason;
    private String recommendType;
    private BigDecimal price;
    private BigDecimal score;
    private String coverImageUrl;
    private String canteenName;
    private String canteenType;
    private String stallName;
    private Boolean isTop;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> images;
}
