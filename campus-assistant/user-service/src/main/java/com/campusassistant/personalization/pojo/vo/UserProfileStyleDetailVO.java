package com.campusassistant.personalization.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "用户个性化样式详情")
@Data
public class UserProfileStyleDetailVO {

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "当前头像地址")
    private String avatar;

    @Schema(description = "当前顶部背景地址")
    private String background;

    @Schema(description = "当前墙纸地址")
    private String wallpaper;

    @Schema(description = "当前资料卡片透明度")
    private BigDecimal cardOpacity;

    @Schema(description = "默认头像列表")
    private List<String> defaultAvatars;

    @Schema(description = "默认背景列表")
    private List<String> defaultBackgrounds;

    @Schema(description = "默认墙纸列表")
    private List<String> defaultWallpapers;
}