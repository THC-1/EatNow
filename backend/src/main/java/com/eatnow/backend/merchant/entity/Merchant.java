package com.eatnow.backend.merchant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long stallId;
    private String name;
    private String description;
    private String logoUrl;
    private String contactPhone;
    private String businessHours;
    private String applyStatus;
    private String status;
    private String rejectReason;
    private Integer favoriteCount;
    private Integer reviewCount;
    private BigDecimal averageScore;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
