package com.eatnow.backend.post.vo;

import lombok.Data;

@Data
public class PostTagRelationVo {

    private Long postId;
    private Long tagId;
    private String tagName;
}
