// 路径：pojo/CourseDTO.java (原来就有的，新增几个字段)
package com.campusassistant.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "课表数据传输对象")
@Data
public class CourseDTO {
    @Schema(description = "学号")
    private String studentId;
    @Schema(description = "学年")
    private String academicYear;
    @Schema(description = "学期")
    private String semester;
    @Schema(description = "课表JSON数据")
    private String scheduleJson;

}
