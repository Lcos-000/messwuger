package com.campusassistant.remote.spider.grades.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class GradesCallbackDTO {

    private String studentId;

    private String academicYear;

    private String semester;

    private List<GradeItemDTO> grades;
}