package com.eatnow.backend.eatlist.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EatListItemVo {

    private Long id;
    private Long userId;
    private Long dishId;
    private String status;
    private Long sourceLotteryRecordId;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime eatenAt;
    private LocalDateTime updatedAt;
    private EatListDishSummaryVo dish;
}
