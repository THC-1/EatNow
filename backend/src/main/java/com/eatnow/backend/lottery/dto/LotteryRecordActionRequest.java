package com.eatnow.backend.lottery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LotteryRecordActionRequest {

    @NotBlank(message = "resultAction must not be blank")
    private String resultAction;
}
