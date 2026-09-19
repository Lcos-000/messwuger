package com.campusassistant.student.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campusassistant.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_deletion_task")
public class UserDeletionTaskEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;
    private String customAvatar;
    private String customBackground;
    private String customWallpaper;
    private String status;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
    private String leaseToken;
    private LocalDateTime leaseUntil;
    private String lastError;
}
