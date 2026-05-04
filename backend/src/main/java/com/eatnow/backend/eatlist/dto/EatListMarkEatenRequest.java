package com.eatnow.backend.eatlist.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EatListMarkEatenRequest {

    @Size(max = 255, message = "note length must be less than or equal to 255")
    private String note;

    private LocalDateTime eatenAt;
}
