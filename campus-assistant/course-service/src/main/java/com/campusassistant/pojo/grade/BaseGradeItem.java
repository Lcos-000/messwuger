package com.campusassistant.pojo.grade;

import lombok.Data;

@Data
public class BaseGradeItem {

    private String studentId;

    private String academicYear;

    private String semester;

    private String courseName;

    private String courseCode;

    private String courseNature;

    private String credit;

    private String score;

    private String gpa;

    private String teacher;

    private String examNature;

    private String courseType;
}