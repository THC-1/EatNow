package com.eatnow.backend.merchant.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MerchantApplyVo {

    private Long merchantId;
    private String applyStatus;
}
