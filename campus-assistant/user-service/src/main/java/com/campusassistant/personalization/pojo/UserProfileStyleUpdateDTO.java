package com.campusassistant.personalization.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Schema(description = "资料卡片透明度，范围 0.00 ~ 1.00")
    @DecimalMin(value = "0.00", message = "资料卡片透明度不能小于 0.00")
    @DecimalMax(value = "1.00", message = "资料卡片透明度不能大于 1.00")
    private BigDecimal cardOpacity;

    @Schema(description = "资料卡片模糊度，范围 0.00 ~ 30.00")
    @DecimalMin(value = "0.00", message = "资料卡片模糊度不能小于 0.00")
    @DecimalMax(value = "30.00", message = "资料卡片模糊度不能大于 30.00")
    private BigDecimal cardBlur;

    @Min(value = 0, message = "全局字体开关取值非法")
    @Max(value = 1, message = "全局字体开关取值非法")
    @Schema(description = "是否启用全局字体：0否 1是")
    private Integer globalFontEnabled;

    @Schema(description = "墙纸蒙版强度，范围 0.00 ~ 1.00")
    @DecimalMin(value = "0.00", message = "墙纸蒙版强度不能小于 0.00")
    @DecimalMax(value = "1.00", message = "墙纸蒙版强度不能大于 1.00")
    private BigDecimal wallpaperMask;
}
