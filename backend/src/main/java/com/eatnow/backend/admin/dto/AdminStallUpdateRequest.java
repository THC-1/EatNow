package com.eatnow.backend.admin.dto;

import lombok.Data;

@Data
public class AdminStallUpdateRequest {

    private String name;
    private String location;
    private String description;
    private String status;
}
