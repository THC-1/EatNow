package com.eatnow.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCanteenCreateRequest {

    @NotNull(message = "campusId must not be null")
    private Long campusId;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "type must not be blank")
    private String type;

    private String location;
    private String description;
    private String openingHours;
}
