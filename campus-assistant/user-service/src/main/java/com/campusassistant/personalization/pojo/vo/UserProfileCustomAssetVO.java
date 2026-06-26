package com.campusassistant.personalization.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户自定义图片资源")
@Data
public class UserProfileCustomAssetVO {

    @Schema(description = "自定义头像")
    private String customAvatar;

    @Schema(description = "自定义顶部背景")
    private String customBackground;

    @Schema(description = "自定义墙纸")
    private String customWallpaper;
}