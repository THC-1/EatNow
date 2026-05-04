package com.eatnow.backend.favorite.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FavoriteCheckVo {

    private Boolean isFavorite;
    private Long favoriteId;
}
