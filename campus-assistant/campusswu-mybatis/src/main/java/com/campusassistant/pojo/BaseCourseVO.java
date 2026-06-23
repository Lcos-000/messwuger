package com.campusassistant.pojo;

import lombok.Data;

@Data
public class BaseCourseVO {
    private String studentId;
    private String academicYear;
    private String semester;
    private String scheduleJson;
}
