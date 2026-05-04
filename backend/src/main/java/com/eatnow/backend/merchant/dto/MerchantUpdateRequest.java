package com.eatnow.backend.merchant.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantUpdateRequest {

    @Size(max = 128, message = "name length must be less than or equal to 128")
    private String name;

    @Size(max = 500, message = "description length must be less than or equal to 500")
    private String description;

    @Size(max = 255, message = "logoUrl length must be less than or equal to 255")
    private String logoUrl;

    @Size(max = 64, message = "businessHours length must be less than or equal to 64")
    private String businessHours;

    @Size(max = 20, message = "contactPhone length must be less than or equal to 20")
    private String contactPhone;
}
