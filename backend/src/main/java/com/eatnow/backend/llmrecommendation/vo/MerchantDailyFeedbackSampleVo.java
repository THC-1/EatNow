package com.eatnow.backend.llmrecommendation.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantDailyFeedbackSampleVo {

    private Long dishId;
    private String dishName;
    private String feedbackType;
    private String status;
    private String content;
    private LocalDateTime createdAt;
}
