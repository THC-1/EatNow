package com.eatnow.backend.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("canteen")
public class Canteen {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long campusId;
    private String name;
    private String type;
    private String location;
    private String description;
    private String openingHours;
    private String contactPhone;
    private BigDecimal averageScore;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
