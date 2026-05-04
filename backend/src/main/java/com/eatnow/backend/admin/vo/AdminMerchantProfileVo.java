package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMerchantProfileVo {

    private Long merchantId;
    private String merchantName;
    private Long stallId;
    private String stallName;
    private Long canteenId;
    private String canteenName;
    private String applyStatus;
    private String merchantStatus;
    private String rejectReason;
    private String description;
    private LocalDateTime approvedAt;
}
