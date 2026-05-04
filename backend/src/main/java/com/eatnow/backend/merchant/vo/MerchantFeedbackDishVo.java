package com.eatnow.backend.merchant.vo;

import lombok.Data;

@Data
public class MerchantFeedbackDishVo {

    private Long dishId;
    private String dishName;
    private Integer feedbackCount;
    private Integer pendingFeedbackCount;
}
