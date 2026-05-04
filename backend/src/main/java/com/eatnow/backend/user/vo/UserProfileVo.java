package com.eatnow.backend.user.vo;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class UserProfileVo {

    Long id;
    String nickname;
    String avatar;
    String phone;
    LocalDateTime createdAt;
}
