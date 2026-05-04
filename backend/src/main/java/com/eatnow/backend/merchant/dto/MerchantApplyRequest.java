package com.eatnow.backend.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantApplyRequest {

    @NotBlank(message = "merchantName must not be blank")
    @Size(max = 128, message = "merchantName length must be less than or equal to 128")
    private String merchantName;

    private Long stallId;

    @Size(max = 32, message = "storeType length must be less than or equal to 32")
    private String storeType;

    private Long campusId;

    @Size(max = 500, message = "description length must be less than or equal to 500")
    private String description;

    @Size(max = 64, message = "businessHours length must be less than or equal to 64")
    private String businessHours;

    @Size(max = 20, message = "contactPhone length must be less than or equal to 20")
    private String contactPhone;
}
