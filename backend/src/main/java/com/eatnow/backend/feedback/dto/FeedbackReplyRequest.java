package com.eatnow.backend.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackReplyRequest {

    @NotBlank(message = "replyContent must not be blank")
    @Size(max = 500, message = "replyContent length must be less than or equal to 500")
    private String replyContent;
}
