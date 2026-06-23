package com.campusassistant.remote.spider.pojo;

import lombok.Data;

@Data
public class PersonalInfoVO {
    private String studentId; // 学号
    private String name;      // 姓名
    private String major;     // 专业
    private String className; // 班级
    private String college; // 学校
}