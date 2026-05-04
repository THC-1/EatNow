package com.eatnow.backend.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long dishId;
    private String targetType;
    private BigDecimal overallScore;
    private BigDecimal tasteScore;
    private BigDecimal portionScore;
    private BigDecimal valueScore;
    private String content;
    private Boolean isAnonymous;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
