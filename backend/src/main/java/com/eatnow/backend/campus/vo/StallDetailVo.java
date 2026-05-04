package com.eatnow.backend.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StallDetailVo {

    private Long id;
    private Long canteenId;
    private String name;
    private String location;
    private String description;
    private String status;
    private Long merchantId;
    private String merchantName;
    private Long dishCount;
    private BigDecimal averageScore;
}
