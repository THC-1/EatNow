package com.eatnow.backend.user.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserPreferenceUpdateRequest {

    private Long defaultCanteenId;
    private String defaultCanteenType;

    @DecimalMin(value = "0.00", message = "minPrice must be greater than or equal to 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.00", message = "maxPrice must be greater than or equal to 0")
    private BigDecimal maxPrice;

    private String tastePreference;
    private String avoidTags;
}
