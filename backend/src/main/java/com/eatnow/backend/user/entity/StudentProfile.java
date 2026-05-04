package com.eatnow.backend.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_profile")
public class StudentProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long campusId;
    private String studentNo;
    private String grade;
    private String major;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
