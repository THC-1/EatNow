package com.eatnow.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantApplicationRejectRequest {

    @NotBlank(message = "rejectReason must not be blank")
    private String rejectReason;
}
