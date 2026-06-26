package com.campusassistant.personalization.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campusassistant.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_profile_custom_asset")
public class UserProfileCustomAssetEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;

    private String customAvatar;

    private String customBackground;

    private String customWallpaper;
}
