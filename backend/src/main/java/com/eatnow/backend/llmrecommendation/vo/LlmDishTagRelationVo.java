package com.eatnow.backend.llmrecommendation.vo;

import lombok.Data;

@Data
public class LlmDishTagRelationVo {

    private Long dishId;
    private Long tagId;
    private String tagName;
}
