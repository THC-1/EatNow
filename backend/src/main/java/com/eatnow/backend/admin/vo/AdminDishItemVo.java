package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminDishItemVo {

    private Long id;
    private Long merchantId;
    private String merchantName;
    private Long canteenId;
    private String canteenName;
    private Long stallId;
    private String stallName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private String coverImageUrl;
    private BigDecimal averageScore;
    private String status;
    private Boolean isJoinLottery;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer reviewCount;
    private LocalDateTime createdAt;
}
