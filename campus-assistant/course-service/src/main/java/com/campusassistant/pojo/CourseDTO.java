// 路径：pojo/CourseDTO.java (原来就有的，新增几个字段)
package com.campusassistant.pojo;

import lombok.Data;

@Data
public class CourseDTO {
    private String studentId;
    private String academicYear;
    private String semester;
    private String scheduleJson;

}
