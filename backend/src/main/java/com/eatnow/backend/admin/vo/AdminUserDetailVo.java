package com.eatnow.backend.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserDetailVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String gender;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> roles;
    private Integer reviewCount;
    private AdminStudentProfileVo studentProfile;
    private AdminMerchantProfileVo merchantProfile;
}
