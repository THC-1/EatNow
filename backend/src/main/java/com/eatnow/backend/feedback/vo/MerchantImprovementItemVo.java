package com.eatnow.backend.feedback.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantImprovementItemVo {

    private Long id;
    private Long dishId;
    private String dishName;
    private Long feedbackId;
    private String feedbackContent;
    private String title;
    private String content;
    private String beforeDescription;
    private String afterDescription;
    private String status;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}
