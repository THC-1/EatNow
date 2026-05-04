package com.eatnow.backend.dish.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DishSaveRequest {

    @NotBlank(message = "name must not be blank")
    @Size(max = 128, message = "name length must be less than or equal to 128")
    private String name;

    @Size(max = 500, message = "description length must be less than or equal to 500")
    private String description;

    @NotNull(message = "price must not be null")
    @DecimalMin(value = "0.01", message = "price must be greater than 0")
    private BigDecimal price;

    private Long categoryId;

    private List<Long> tagIds;

    private List<String> images;

    private Boolean isJoinLottery;
}
