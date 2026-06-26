package com.campusassistant.personalization.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户个性化样式信息")
@Data
public class UserProfileStyleVO {

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "顶部背景地址")
    private String background;

    @Schema(description = "墙纸地址")
    private String wallpaper;

    @Schema(description = "资料卡片透明度")
    private BigDecimal cardOpacity;

    @Schema(description = "资料卡片模糊度")
    private BigDecimal cardBlur;

    @Schema(description = "是否启用全局字体：0否 1是")
    private Integer globalFontEnabled;

    @Schema(description = "墙纸蒙版强度")
    private BigDecimal wallpaperMask;
}