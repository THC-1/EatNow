package com.eatnow.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDishStatusUpdateRequest {

    @NotBlank(message = "status must not be blank")
    private String status;
}
