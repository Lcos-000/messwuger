package com.campusassistant.student.pojo;

import lombok.Data;

@Data
public class UserStatusVO {
    private String studentId; // 学号
    private Integer syncStatus; // 同步状态
    private Integer punchStatus; // 打卡状态
}