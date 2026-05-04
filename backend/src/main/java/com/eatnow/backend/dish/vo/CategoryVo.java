package com.eatnow.backend.dish.vo;

import lombok.Data;

@Data
public class CategoryVo {

    private Long id;
    private String name;
    private String type;
    private Integer sortOrder;
    private String status;
}
