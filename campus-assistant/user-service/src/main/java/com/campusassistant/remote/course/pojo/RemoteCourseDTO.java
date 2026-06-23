package com.campusassistant.remote.course.pojo;

import lombok.Data;

@Data
public class RemoteCourseDTO {
    private String studentId;
    private String academicYear;
    private String semester;
    private String scheduleJson;

}
