package com.eatnow.backend.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_post_image")
public class StudentPostImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
