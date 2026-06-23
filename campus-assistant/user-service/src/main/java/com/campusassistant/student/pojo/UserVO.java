package com.campusassistant.student.pojo;

import com.campusassistant.pojo.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserVO extends BaseEntity {
    private String studentId; // 学号

    private String name;      // 姓名

    private String major;     // 专业

    private String className; // 班级

    private Integer syncStatus; // 同步状态，前端轮询靠它来判断是否跳转课表页

    private Integer punchStatus;

    private String role;      // 角色权限

}