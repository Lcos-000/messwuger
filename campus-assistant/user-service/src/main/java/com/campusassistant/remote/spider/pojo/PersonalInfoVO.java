package com.campusassistant.remote.spider.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户个人信息")
@Data
public class PersonalInfoVO {
    @Schema(description = "学号")
    private String studentId;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "专业")
    private String major;
    @Schema(description = "班级")
    private String className;
    @Schema(description = "学校/学院")
    private String college;
}