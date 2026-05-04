package com.eatnow.backend.merchant.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantProfileVo {

    private Long id;
    private Long userId;
    private Long stallId;
    private Long canteenId;
    private String name;
    private String description;
    private String logoUrl;
    private String contactPhone;
    private String businessHours;
    private String applyStatus;
    private String status;
    private String rejectReason;
    private String canteenName;
    private String canteenType;
    private String stallName;
    private BigDecimal averageScore;
    private Long dishCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
