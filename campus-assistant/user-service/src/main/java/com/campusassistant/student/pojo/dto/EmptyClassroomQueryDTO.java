package com.campusassistant.student.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmptyClassroomQueryDTO {


    @NotBlank(message = "academicYear不能为空")
    private String academicYear;

    @NotBlank(message = "semester不能为空")
    @Pattern(regexp = "^(3|6|12)$", message = "semester只能是3、6或12")
    private String semester;

    @NotBlank(message = "dayOfWeek不能为空")
    @Pattern(regexp = "^[1-7]$", message = "dayOfWeek只能是1到7的单个数字")
    private String dayOfWeek;

    @NotBlank(message = "periodsMask不能为空")
    private String periodsMask;

    @NotBlank(message = "weeksMask不能为空")
    private String weeksMask;

    private String campusId;

    private String building;

    private String roomType;
}