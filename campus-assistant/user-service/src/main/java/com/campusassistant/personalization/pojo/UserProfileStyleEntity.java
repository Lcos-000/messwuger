package com.campusassistant.personalization.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campusassistant.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_profile_style")
public class UserProfileStyleEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
     //学号
    private String studentId;
     //头像地址
    private String avatar;
    //顶部背景地址
    private String background;
    //墙纸地址
    private String wallpaper;
    //资料卡片透明度
    private BigDecimal cardOpacity;

}
