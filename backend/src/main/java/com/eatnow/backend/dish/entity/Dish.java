package com.eatnow.backend.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private Long canteenId;
    private Long stallId;
    private Long categoryId;
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
}
