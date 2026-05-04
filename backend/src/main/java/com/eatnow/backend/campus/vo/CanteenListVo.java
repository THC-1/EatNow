package com.eatnow.backend.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CanteenListVo {

    private Long id;
    private Long campusId;
    private String name;
    private String type;
    private String location;
    private String description;
    private String openingHours;
    private String status;
    private BigDecimal averageScore;
}
