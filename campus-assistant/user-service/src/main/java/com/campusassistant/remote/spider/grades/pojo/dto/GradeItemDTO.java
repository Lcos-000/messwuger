package com.campusassistant.remote.spider.grades.pojo.dto;

import lombok.Data;

@Data
public class GradeItemDTO {
    // 课程名称
    private String courseName;
    // 课程编码
    private String courseCode;
    // 课程性质 必修/选修
    private String courseNature;
    // 学分
    private String credit;
    // 成绩
    private String score;
    // GPA
    private String gpa;
    // 教师
    private String teacher;
    // 考试性质
    private String examNature;
    // 课程类型
    private String courseType;
    // 学年
    private String academicYear;
    // 学期
    private String semester;
}