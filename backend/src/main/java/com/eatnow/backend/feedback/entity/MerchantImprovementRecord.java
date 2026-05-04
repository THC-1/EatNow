package com.eatnow.backend.feedback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant_improvement_record")
public class MerchantImprovementRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private Long dishId;
    private Long feedbackId;
    private String title;
    private String content;
    private String beforeDescription;
    private String afterDescription;
    private String status;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
