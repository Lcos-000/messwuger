package com.campusassistant.personalization.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "更新用户个性化样式请求参数")
@Data
public class UserProfileStyleUpdateDTO {

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "顶部背景地址")
    private String background;

    @Schema(description = "墙纸地址")
    private String wallpaper;

    @Schema(description = "资料卡片透明度，范围 0.20 ~ 1.00")
    @DecimalMin(value = "0.20", message = "资料卡片透明度不能小于 0.20")
    @DecimalMax(value = "1.00", message = "资料卡片透明度不能大于 1.00")
    private BigDecimal cardOpacity;
}
