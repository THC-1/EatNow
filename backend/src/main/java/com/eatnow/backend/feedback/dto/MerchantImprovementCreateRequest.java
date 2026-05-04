package com.eatnow.backend.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantImprovementCreateRequest {

    private Long dishId;

    private Long feedbackId;

    @NotBlank(message = "title must not be blank")
    @Size(max = 128, message = "title length must be less than or equal to 128")
    private String title;

    @NotBlank(message = "content must not be blank")
    @Size(max = 5000, message = "content length must be less than or equal to 5000")
    private String content;

    @Size(max = 255, message = "beforeDescription length must be less than or equal to 255")
    private String beforeDescription;

    @Size(max = 255, message = "afterDescription length must be less than or equal to 255")
    private String afterDescription;

    @Size(max = 16, message = "status length must be less than or equal to 16")
    private String status;

    private Boolean isPublic;
}
