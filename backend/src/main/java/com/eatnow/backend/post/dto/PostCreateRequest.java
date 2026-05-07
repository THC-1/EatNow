package com.eatnow.backend.post.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PostCreateRequest {

    @NotBlank(message = "foodName must not be blank")
    @Size(max = 128, message = "foodName length must be less than or equal to 128")
    private String foodName;

    @NotBlank(message = "shopName must not be blank")
    @Size(max = 128, message = "shopName length must be less than or equal to 128")
    private String shopName;

    @NotBlank(message = "content must not be blank")
    @Size(max = 5000, message = "content length must be less than or equal to 5000")
    private String content;

    @Size(max = 128, message = "title length must be less than or equal to 128")
    private String title;

    private Long canteenId;

    private Long stallId;

    private Long categoryId;

    @DecimalMin(value = "0.00", message = "price must be greater than or equal to 0")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "score must be greater than or equal to 0")
    @DecimalMax(value = "5.00", message = "score must be less than or equal to 5")
    private BigDecimal score;

    private List<String> images;

    private List<Long> tagIds;

    private Boolean isJoinLottery;
}
