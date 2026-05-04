package com.eatnow.backend.merchantrecommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantRecommendationUpdateRequest {

    @NotBlank(message = "title must not be blank")
    @Size(max = 128, message = "title length must be less than or equal to 128")
    private String title;

    @Size(max = 500, message = "recommendReason length must be less than or equal to 500")
    private String recommendReason;

    @NotBlank(message = "recommendType must not be blank")
    @Size(max = 32, message = "recommendType length must be less than or equal to 32")
    private String recommendType;

    @NotNull(message = "startTime must not be null")
    private LocalDateTime startTime;

    @NotNull(message = "endTime must not be null")
    private LocalDateTime endTime;

    private Boolean isTop;
}
