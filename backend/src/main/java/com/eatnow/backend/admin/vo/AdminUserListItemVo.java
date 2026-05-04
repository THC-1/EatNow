package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserListItemVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
