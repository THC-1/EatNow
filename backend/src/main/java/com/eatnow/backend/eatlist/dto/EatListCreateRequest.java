package com.eatnow.backend.eatlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EatListCreateRequest {

    @NotNull(message = "dishId must not be null")
    private Long dishId;

    private Long sourceLotteryRecordId;

    @Size(max = 255, message = "note length must be less than or equal to 255")
    private String note;
}
