package com.eatnow.backend.dish.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DishStatusUpdateRequest {

    @NotBlank(message = "status must not be blank")
    private String status;
}
