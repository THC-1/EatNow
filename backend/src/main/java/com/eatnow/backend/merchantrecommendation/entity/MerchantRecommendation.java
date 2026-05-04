package com.eatnow.backend.merchantrecommendation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant_recommendation")
public class MerchantRecommendation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private Long dishId;
    private String title;
    private String recommendReason;
    private String recommendType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Boolean isTop;
    private Integer clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
