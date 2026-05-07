package com.eatnow.backend.campus.vo;

import lombok.Data;

@Data
public class CampusPlaceSearchVo {

    private String placeType;
    private Long id;
    private Long canteenId;
    private Long stallId;
    private String name;
    private String canteenName;
    private String canteenType;
    private String location;
    private String description;
    private String merchantName;
}
