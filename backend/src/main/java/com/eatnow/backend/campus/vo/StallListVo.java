package com.eatnow.backend.campus.vo;

import lombok.Data;

@Data
public class StallListVo {

    private Long id;
    private Long canteenId;
    private String name;
    private String location;
    private String description;
    private String status;
    private String merchantName;
}
