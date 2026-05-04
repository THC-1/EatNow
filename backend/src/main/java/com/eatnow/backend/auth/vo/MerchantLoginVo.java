package com.eatnow.backend.auth.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MerchantLoginVo {

    Long userId;
    Long merchantId;
    String applyStatus;
    String merchantStatus;
    @JsonUnwrapped
    TokenPairVo tokenPair;
}
