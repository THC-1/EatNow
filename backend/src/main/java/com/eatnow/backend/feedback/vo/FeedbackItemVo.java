package com.eatnow.backend.feedback.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackItemVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String targetType;
    private Long targetId;
    private Long dishId;
    private String dishName;
    private String feedbackType;
    private String content;
    private String status;
    private String replyContent;
    private Long repliedBy;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
