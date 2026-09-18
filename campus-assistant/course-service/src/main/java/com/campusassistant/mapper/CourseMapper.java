// 路径：mapper/CourseMapper.java
package com.campusassistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusassistant.pojo.schedule.CourseEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<CourseEntity> {

    @Insert("""
            INSERT INTO course_db (student_id, academic_year, semester, schedule_json)
            VALUES (#{studentId}, #{academicYear}, #{semester}, #{scheduleJson})
            ON DUPLICATE KEY UPDATE
                schedule_json = VALUES(schedule_json),
                update_time = CURRENT_TIMESTAMP
            """)
    int upsertSchedule(CourseEntity courseEntity);
}
