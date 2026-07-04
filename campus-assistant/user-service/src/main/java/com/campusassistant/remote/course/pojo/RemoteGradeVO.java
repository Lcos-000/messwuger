package com.campusassistant.remote.course.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RemoteGradeVO {

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

    private LocalDateTime syncTime;
}