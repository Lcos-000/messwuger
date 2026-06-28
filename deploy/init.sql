-- 校园助手数据库初始化脚本
-- 会被 docker-compose.middleware.yml 挂载到 MySQL 容器 /docker-entrypoint-initdb.d/ 自动执行

CREATE DATABASE IF NOT EXISTS campus_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_db;

CREATE TABLE IF NOT EXISTS student_db (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL COMMENT '教务学号',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt加密后的密码',
    sync_status TINYINT DEFAULT 0 COMMENT '0未同步 1同步中 2成功 3失败',
    punch_status TINYINT DEFAULT 0 COMMENT '打卡状态：0未打卡 1打卡中 2打卡成功 3打卡失败',
    auto_punch_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启自动打卡：0关闭 1开启',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS personal_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL COMMENT '学号',
    name VARCHAR(64) DEFAULT NULL COMMENT '姓名',
    major VARCHAR(128) DEFAULT NULL COMMENT '专业',
    class_name VARCHAR(128) DEFAULT NULL COMMENT '班级',
    college VARCHAR(128) DEFAULT NULL COMMENT '学院',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人信息表';

CREATE TABLE IF NOT EXISTS course_db (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL COMMENT '学号',
    academic_year VARCHAR(16) DEFAULT NULL COMMENT '学年',
    semester VARCHAR(8) DEFAULT NULL COMMENT '学期',
    schedule_json LONGTEXT COMMENT '课表JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表数据表';

CREATE TABLE IF NOT EXISTS user_profile_style (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL COMMENT '学号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址，可为空，空时前端使用姓名首字母兜底',
    background VARCHAR(255) DEFAULT NULL COMMENT '顶部背景地址，可为空，空时前端使用纯白极简背景',
    wallpaper VARCHAR(255) DEFAULT NULL COMMENT '墙纸地址，可为空，空时前端使用浅灰极简背景',
    card_opacity DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '资料卡透明度',
    card_blur INT DEFAULT 14 COMMENT '资料卡模糊度',
    wallpaper_mask DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '墙纸蒙版强度(0.00-1.00)',
    global_font_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用全局字体：0关闭 1开启',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个性化配置表';

CREATE TABLE IF NOT EXISTS user_profile_custom_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL COMMENT '学号',
    custom_avatar VARCHAR(255) DEFAULT NULL COMMENT '自定义头像 OSS 地址',
    custom_background VARCHAR(255) DEFAULT NULL COMMENT '自定义顶部背景 OSS 地址',
    custom_wallpaper VARCHAR(255) DEFAULT NULL COMMENT '自定义墙纸 OSS 地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义图片资源表';
