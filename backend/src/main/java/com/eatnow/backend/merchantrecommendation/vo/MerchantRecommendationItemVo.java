package com.eatnow.backend.merchantrecommendation.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantRecommendationItemVo {

    private Long id;
    private Long merchantId;
    private Long dishId;
    private String dishName;
    private String title;
    private String recommendReason;
    private String recommendType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Boolean isTop;
    private Integer clickCount;
    private LocalDateTime createdAt;
}
