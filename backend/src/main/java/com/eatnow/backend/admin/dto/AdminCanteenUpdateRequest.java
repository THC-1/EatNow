package com.eatnow.backend.admin.dto;

import lombok.Data;

@Data
public class AdminCanteenUpdateRequest {

    private Long campusId;
    private String name;
    private String type;
    private String location;
    private String description;
    private String openingHours;
    private String status;
}
