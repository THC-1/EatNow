package com.eatnow.backend.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_preference")
public class UserPreference {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long defaultCanteenId;
    private String defaultCanteenType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String tastePreference;
    private String avoidTags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
