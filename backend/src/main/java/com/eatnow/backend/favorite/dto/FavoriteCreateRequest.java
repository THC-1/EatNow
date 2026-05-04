package com.eatnow.backend.favorite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteCreateRequest {

    @NotBlank(message = "targetType must not be blank")
    private String targetType;

    @NotNull(message = "targetId must not be null")
    private Long targetId;
}
