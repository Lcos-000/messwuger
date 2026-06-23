// 路径：pojo/CourseEntity.java (用来映射数据库表)
package com.campusassistant.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.campusassistant.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("course_db") // 或者你自定义的表名
public class CourseEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 绑定哪个学生
    private String studentId;

    // 学年，如 "2025-2026"
    private String academicYear;

    // 学期代码，如 "12"
    private String semester;

    // 核心字段：直接存 Go 端传过来的整个课表 JSON 数组字符串！极速快跑！
    private String scheduleJson;

}