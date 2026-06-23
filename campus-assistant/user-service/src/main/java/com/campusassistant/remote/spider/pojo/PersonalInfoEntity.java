package com.campusassistant.remote.spider.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("personal_info")
public class PersonalInfoEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentId;
    private String name;
    private String major;
    private String className;
    private String college = "西南大学";
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
