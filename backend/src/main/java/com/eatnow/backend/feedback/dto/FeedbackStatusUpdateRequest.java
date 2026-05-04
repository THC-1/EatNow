package com.eatnow.backend.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackStatusUpdateRequest {

    @NotBlank(message = "status must not be blank")
    @Size(max = 16, message = "status length must be less than or equal to 16")
    private String status;
}
