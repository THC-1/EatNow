package com.eatnow.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminStallCreateRequest {

    @NotNull(message = "canteenId must not be null")
    private Long canteenId;

    @NotBlank(message = "name must not be blank")
    private String name;

    private String location;
    private String description;
}
