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
