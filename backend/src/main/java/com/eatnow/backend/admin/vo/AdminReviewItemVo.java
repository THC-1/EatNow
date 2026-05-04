package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminReviewItemVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String targetType;
    private Long targetId;
    private Long dishId;
    private String dishName;
    private String merchantName;
    private BigDecimal overallScore;
    private String content;
    private Boolean isAnonymous;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    private List<String> images;
}
