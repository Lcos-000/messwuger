package com.campusassistant.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campusassistant.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("student_grade")
public class GradeEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    // 学号
    private String studentId;
    // 学年，例如 2025
    private String academicYear;
    // 学期，例如 12 / 3
    private String semester;
    // 课程名称
    private String courseName;
    // 课程代码
    private String courseCode;
    //课程性质
    private String courseNature;
    // 学分
    private String credit;
    // 成绩
    private String score;
    // 绩点
    private String gpa;
    // 任课教师
    private String teacher;
    // 考试性质
    private String examNature;
    // 课程类别
    private String courseType;
    // 本次同步时间
    private LocalDateTime syncTime;
}