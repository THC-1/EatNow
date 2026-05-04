package com.eatnow.backend.review.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewItemVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String targetType;
    private Long targetId;
    private BigDecimal overallScore;
    private BigDecimal tasteScore;
    private BigDecimal portionScore;
    private BigDecimal valueScore;
    private String content;
    private Boolean isAnonymous;
    private Integer likeCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private List<String> images;
    private Long dishId;
    private String dishName;
}
