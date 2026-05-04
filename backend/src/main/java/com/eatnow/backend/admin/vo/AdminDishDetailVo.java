package com.eatnow.backend.admin.vo;

import com.eatnow.backend.dish.vo.DishTagVo;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminDishDetailVo {

    private Long id;
    private Long merchantId;
    private String merchantName;
    private String merchantStatus;
    private String merchantApplyStatus;
    private Long canteenId;
    private String canteenName;
    private Long stallId;
    private String stallName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private String coverImageUrl;
    private BigDecimal averageScore;
    private BigDecimal tasteScore;
    private BigDecimal portionScore;
    private BigDecimal valueScore;
    private String status;
    private Boolean isJoinLottery;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images;
    private List<DishTagVo> tags;
}
