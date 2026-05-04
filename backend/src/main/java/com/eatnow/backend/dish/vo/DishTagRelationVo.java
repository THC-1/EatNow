package com.eatnow.backend.dish.vo;

import lombok.Data;

@Data
public class DishTagRelationVo {

    private Long dishId;
    private Long tagId;
    private String tagName;
}
