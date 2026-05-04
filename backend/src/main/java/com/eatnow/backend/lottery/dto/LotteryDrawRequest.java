package com.eatnow.backend.lottery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LotteryDrawRequest {

    @NotBlank(message = "drawMode must not be blank")
    private String drawMode;
}
