package com.eatnow.backend.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackCreateRequest {

    @NotBlank(message = "targetType must not be blank")
    @Size(max = 16, message = "targetType length must be less than or equal to 16")
    private String targetType;

    @NotNull(message = "targetId must not be null")
    private Long targetId;

    @NotBlank(message = "feedbackType must not be blank")
    @Size(max = 32, message = "feedbackType length must be less than or equal to 32")
    private String feedbackType;

    @NotBlank(message = "content must not be blank")
    @Size(max = 5000, message = "content length must be less than or equal to 5000")
    private String content;
}
