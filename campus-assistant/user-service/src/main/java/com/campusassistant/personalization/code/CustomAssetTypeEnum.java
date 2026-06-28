package com.campusassistant.personalization.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomAssetTypeEnum {

    AVATAR("avatar", "customAvatar", "avatar"),
    BACKGROUND("background", "customBackground", "background"),
    WALLPAPER("wallpaper", "customWallpaper", "wallpaper");

    private final String code;
    private final String fieldName;
    private final String ossFolder;

    public static CustomAssetTypeEnum fromCode(String code) {
        for (CustomAssetTypeEnum value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
