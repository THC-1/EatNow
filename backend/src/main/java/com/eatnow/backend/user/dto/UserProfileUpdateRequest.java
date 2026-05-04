package com.eatnow.backend.user.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    private String nickname;
    private String avatar;
    private String phone;
}
