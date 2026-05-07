package com.eatnow.backend.llmrecommendation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LlmRecommendationConstraints {

    private Long canteenId;
    private Long categoryId;

    @Size(max = 10, message = "tagIds size must be less than or equal to 10")
    private List<Long> tagIds;

    @DecimalMin(value = "0.00", message = "minPrice must be greater than or equal to 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.00", message = "maxPrice must be greater than or equal to 0")
    private BigDecimal maxPrice;
}
