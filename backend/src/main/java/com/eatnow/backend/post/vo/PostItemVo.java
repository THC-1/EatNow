package com.eatnow.backend.post.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostItemVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long canteenId;
    private String canteenName;
    private String canteenType;
    private Long stallId;
    private String stallName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String foodName;
    private String shopName;
    private String content;
    private BigDecimal price;
    private BigDecimal score;
    private String coverImageUrl;
    private Boolean isJoinLottery;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Boolean isLiked;
    private List<String> images;
    private List<PostTagVo> tags;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
