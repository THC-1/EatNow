package com.eatnow.backend.admin.vo;

import lombok.Data;

@Data
public class AdminStatisticsOverviewVo {

    private Long studentCount;
    private Long merchantCount;
    private Long dishCount;
    private Long reviewCount;
    private Long todayLotteryCount;
    private Long todayActiveUsers;
}
