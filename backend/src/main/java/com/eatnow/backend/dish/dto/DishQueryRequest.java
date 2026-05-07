package com.eatnow.backend.dish.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishQueryRequest {

    private Long canteenId;
    private Long stallId;
    private Long merchantId;
    private Long categoryId;
    private Long tagId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minScore;
    private String keyword;
    private String status;
    private Boolean manage;

    @Min(value = 1, message = "page must be greater than or equal to 1")
    private Integer page = 1;

    @Min(value = 1, message = "size must be greater than or equal to 1")
    @Max(value = 50, message = "size must be less than or equal to 50")
    private Integer size = 10;
}
