package com.campusassistant.pojo.grade.dto;

import lombok.Data;

import java.util.List;

@Data
public class GradeDTO {

    private String studentId;

    private String academicYear;

    private String semester;

    private List<GradeItemDTO> grades;
}