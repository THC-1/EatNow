package com.eatnow.backend.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_post")
public class StudentPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long canteenId;
    private Long stallId;
    private Long categoryId;
    private String title;
    private String dishName;
    private String shopName;
    private String content;
    private BigDecimal price;
    private BigDecimal score;
    private String coverImageUrl;
    private Boolean isJoinLottery;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
