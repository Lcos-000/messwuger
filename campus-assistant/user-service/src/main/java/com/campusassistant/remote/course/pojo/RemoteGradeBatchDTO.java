package com.campusassistant.remote.course.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RemoteGradeBatchDTO {

    private String studentId;

    private String academicYear;

    private String semester;

    private List<RemoteGradeDTO> grades;
}