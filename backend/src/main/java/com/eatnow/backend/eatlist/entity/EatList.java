package com.eatnow.backend.eatlist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("eat_list")
public class EatList {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long dishId;
    private String status;
    private Long sourceLotteryRecordId;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime eatenAt;
    private LocalDateTime updatedAt;
}
