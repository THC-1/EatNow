package com.eatnow.backend.lottery.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LotteryConditionDrawRequest {

    @NotBlank(message = "drawMode must not be blank")
    private String drawMode;

    private Long canteenId;

    @DecimalMin(value = "0.00", message = "minPrice must be greater than or equal to 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.00", message = "maxPrice must be greater than or equal to 0")
    private BigDecimal maxPrice;

    private List<Long> tagIds;

    @DecimalMin(value = "0.00", message = "minScore must be greater than or equal to 0")
    @DecimalMax(value = "5.00", message = "minScore must be less than or equal to 5")
    private BigDecimal minScore;
}
