package com.campusassistant.student.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.campusassistant.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("student_db") //表名对不上时用映射
public class UserEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // --- 核心账号信息 ---
    private String studentId; // 教务学号（作为平台的唯一登录账号）
    private String password;  // 加密后的教务密码（不丢弃，用于后续自动打卡/成绩更新）

    // --- 业务与系统字段 ---
    /**
     * 数据同步状态
     * 0 = 未同步/未绑定
     * 1 = 同步中 (Go 端正在爬取)
     * 2 = 同步成功
     * 3 = 同步失败
     */
    private Integer syncStatus;

    /**
     * 打卡状态
     * 0-未打卡
     * 1-打卡中
     * 2-打卡成功
     * 3-打卡失败
     */
    private Integer punchStatus;

    /**
     * 是否开启自动打卡
     * 0-关闭
     * 1-开启
     */
    private Integer autoPunchEnabled;

    private String role;

}
