package com.eatnow.backend.admin.vo;

import lombok.Data;

@Data
public class AdminStudentProfileVo {

    private Long campusId;
    private String studentNo;
    private String grade;
    private String major;
    private String bio;
}
