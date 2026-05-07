package com.eatnow.backend.llmrecommendation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LlmRecommendationChatRequest {

    @Size(max = 64, message = "conversationId length must be less than or equal to 64")
    private String conversationId;

    @NotBlank(message = "message must not be blank")
    @Size(max = 1000, message = "message length must be less than or equal to 1000")
    private String message;

    private Integer limit;

    @Valid
    private LlmRecommendationConstraints constraints;
}
