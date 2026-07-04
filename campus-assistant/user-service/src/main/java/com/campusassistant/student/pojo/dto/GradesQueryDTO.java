package com.campusassistant.student.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GradesQueryDTO {

    @NotBlank(message = "academicYear不能为空")
    private String academicYear;

    @NotBlank(message = "semester不能为空")
    private String semester;
}