// 路径：service/UserCourseService.java
package com.campusassistant.service;


import com.campusassistant.pojo.CourseDTO;
import com.campusassistant.pojo.CourseVO;

public interface UserCourseService {

    /**
     * 保存或更新课表数据 (供内部调用)
     */
    void saveOrUpdateSchedule(CourseDTO courseDTO);

    /**
     * 查询指定学期的课表
     */
    CourseVO getSchedule();

    /**
     * 查询当前最新的一份课表 (通常前端主页最常用)
     */
    CourseVO getCurrentSchedule();

    CourseVO getScheduleByStudentId(String studentId);

}
