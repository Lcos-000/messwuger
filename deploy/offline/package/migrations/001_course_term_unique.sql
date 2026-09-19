USE campus_db;

DROP PROCEDURE IF EXISTS migrate_course_term;
DELIMITER $$

CREATE PROCEDURE migrate_course_term()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM course_db
        WHERE academic_year IS NULL OR semester IS NULL
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'course_db contains rows with NULL academic_year or semester';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM course_db
        GROUP BY student_id, academic_year, semester
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'course_db contains duplicate student-term rows';
    END IF;

    ALTER TABLE course_db
        MODIFY academic_year VARCHAR(16) NOT NULL,
        MODIFY semester VARCHAR(8) NOT NULL;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'course_db'
          AND index_name = 'uk_course_student_term'
    ) THEN
        ALTER TABLE course_db
            ADD UNIQUE KEY uk_course_student_term (student_id, academic_year, semester);
    END IF;
END$$

DELIMITER ;
CALL migrate_course_term();
DROP PROCEDURE migrate_course_term;

CREATE TABLE IF NOT EXISTS user_deletion_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL COMMENT '待清理用户学号',
    custom_avatar VARCHAR(255) DEFAULT NULL,
    custom_background VARCHAR(255) DEFAULT NULL,
    custom_wallpaper VARCHAR(255) DEFAULT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME NOT NULL,
    lease_token VARCHAR(64) DEFAULT NULL,
    lease_until DATETIME DEFAULT NULL,
    last_error VARCHAR(500) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_deletion_status (status, next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户注销后的跨服务清理任务';

DROP PROCEDURE IF EXISTS migrate_user_deletion_task;
DELIMITER $$

CREATE PROCEDURE migrate_user_deletion_task()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'user_deletion_task'
          AND column_name = 'lease_token'
    ) THEN
        ALTER TABLE user_deletion_task
            ADD lease_token VARCHAR(64) DEFAULT NULL AFTER next_retry_at;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'user_deletion_task'
          AND column_name = 'lease_until'
    ) THEN
        ALTER TABLE user_deletion_task
            ADD lease_until DATETIME DEFAULT NULL AFTER lease_token;
    END IF;
END$$

DELIMITER ;
CALL migrate_user_deletion_task();
DROP PROCEDURE migrate_user_deletion_task;
