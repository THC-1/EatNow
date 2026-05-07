package com.eatnow.backend.post.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostTagVo {

    private Long tagId;
    private String tagName;
}
