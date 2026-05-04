package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMerchantApplicationItemVo {

    private Long merchantId;
    private Long userId;
    private String merchantName;
    private Long stallId;
    private String stallName;
    private Long canteenId;
    private String canteenName;
    private String canteenType;
    private String description;
    private String applyStatus;
    private String rejectReason;
    private LocalDateTime createdAt;
}
