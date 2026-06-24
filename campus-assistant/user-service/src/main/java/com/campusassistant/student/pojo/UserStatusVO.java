package com.campusassistant.student.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户状态信息")
@Data
public class UserStatusVO {
    @Schema(description = "学号")
    private String studentId;
    @Schema(description = "同步状态：0-未同步 1-同步中 2-同步成功 3-同步失败")
    private Integer syncStatus;
    @Schema(description = "打卡状态：0-未打卡 1-打卡中 2-打卡成功 3-打卡失败")
    private Integer punchStatus;
}