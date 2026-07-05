// 路径：service/UserCourseService.java
package com.campusassistant.service;


import com.campusassistant.pojo.grade.dto.GradeDTO;
import com.campusassistant.pojo.grade.GradeVO;
import com.campusassistant.pojo.schedule.CourseDTO;
import com.campusassistant.pojo.schedule.CourseVO;

import java.util.List;

public interface UserCourseService {

    // 保存或更新课表数据 (供内部调用)
    void saveOrUpdateSchedule(CourseDTO courseDTO);

    // 查询指定学期的课表
    CourseVO getSchedule();

    // 查询当前最新的一份课表 (通常前端主页最常用)
    CourseVO getCurrentSchedule();

    // 查询课表
    CourseVO getScheduleByStudentId(String studentId);

    // 保存或覆盖某学期成绩数据（供内部调用）
    void saveOrUpdateGrades(GradeDTO gradeDTO);

    // 根据学号、学年、学期查询成绩（供内部调用）
    List<GradeVO> getGradesByStudentId(String studentId, String academicYear, String semester);

}
