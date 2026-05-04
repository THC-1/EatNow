package com.eatnow.backend.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReviewCreateRequest {

    @NotBlank(message = "targetType must not be blank")
    private String targetType;

    @NotNull(message = "targetId must not be null")
    private Long targetId;

    @NotNull(message = "overallScore must not be null")
    @DecimalMin(value = "0.00", message = "overallScore must be greater than or equal to 0")
    @DecimalMax(value = "5.00", message = "overallScore must be less than or equal to 5")
    private BigDecimal overallScore;

    @NotNull(message = "tasteScore must not be null")
    @DecimalMin(value = "0.00", message = "tasteScore must be greater than or equal to 0")
    @DecimalMax(value = "5.00", message = "tasteScore must be less than or equal to 5")
    private BigDecimal tasteScore;

    @NotNull(message = "portionScore must not be null")
    @DecimalMin(value = "0.00", message = "portionScore must be greater than or equal to 0")
    @DecimalMax(value = "5.00", message = "portionScore must be less than or equal to 5")
    private BigDecimal portionScore;

    @NotNull(message = "valueScore must not be null")
    @DecimalMin(value = "0.00", message = "valueScore must be greater than or equal to 0")
    @DecimalMax(value = "5.00", message = "valueScore must be less than or equal to 5")
    private BigDecimal valueScore;

    @Size(max = 5000, message = "content length must be less than or equal to 5000")
    private String content;

    private List<String> images;

    private Boolean isAnonymous;
}
