package com.eatnow.backend.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_post_tag")
public class StudentPostTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long tagId;
    private LocalDateTime createdAt;
}
