package com.campusassistant.remote.spider.grades.pojo.dto;

import lombok.Data;

@Data
public class GradesTaskSubmitDTO {

    // 学年，例如：2025
    private String academicYear;

    // 学期，例如：12 / 3
    private String semester;

}