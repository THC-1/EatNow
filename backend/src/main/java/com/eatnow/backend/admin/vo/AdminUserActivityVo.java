package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminUserActivityVo {

    private LocalDate date;
    private Long activeUsers;
    private Long lotteryCount;
    private Long reviewCount;
}
