package com.eatnow.backend.feedback.vo;

import lombok.Data;

@Data
public class FeedbackTargetRelationVo {

    private Long dishId;
    private Long merchantId;
    private String dishName;
}
