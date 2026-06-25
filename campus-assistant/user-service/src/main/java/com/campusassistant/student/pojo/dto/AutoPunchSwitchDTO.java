package com.campusassistant.student.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "自动打卡开关请求参数")
public class AutoPunchSwitchDTO {

    @NotNull
    @Min(0)
    @Max(1)
    @Schema(description = "是否开启自动打卡：0-关闭，1-开启")
    private Integer autoPunchEnabled;

}
