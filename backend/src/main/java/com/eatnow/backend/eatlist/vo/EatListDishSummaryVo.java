package com.eatnow.backend.eatlist.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EatListDishSummaryVo {

    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal score;
    private String merchantName;
    private String canteenName;
    private List<String> images;
}
