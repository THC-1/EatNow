package com.eatnow.backend.favorite.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteItemVo {

    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;
    private FavoriteDishSummaryVo dish;
    private FavoritePostSummaryVo post;
}
