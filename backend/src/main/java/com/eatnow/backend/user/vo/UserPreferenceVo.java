package com.eatnow.backend.user.vo;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class UserPreferenceVo {

    Long id;
    Long userId;
    Long defaultCanteenId;
    String defaultCanteenType;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    String tastePreference;
    String avoidTags;
}
