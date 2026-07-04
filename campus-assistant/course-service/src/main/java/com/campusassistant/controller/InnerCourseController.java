// 路径：controller/InnerCourseController.java (对内部微服务暴露)
package com.campusassistant.controller;

import com.campusassistant.pojo.GradeDTO;
import com.campusassistant.pojo.GradeVO;
import com.campusassistant.pojo.schedule.CourseDTO;
import com.campusassistant.pojo.schedule.CourseVO;
import com.campusassistant.pojo.Result;
import com.campusassistant.service.UserCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inner")
@RequiredArgsConstructor
@Tag(name = "课表内部接口")
public class InnerCourseController {

    private final UserCourseService userCourseService;

    /**
     * 提供给 user-service 回调后，通过 OpenFeign 调用的内部写库接口
     */
    @Operation(summary = "同步课表数据（内部调用）")
    @PostMapping("/course/sync")
    public Result<String> syncScheduleData(@RequestBody CourseDTO courseDTO) {
        userCourseService.saveOrUpdateSchedule(courseDTO);
        return Result.success("课表同步入库成功");
    }

    @Operation(summary = "根据学号获取课表（内部调用）")
    @GetMapping("/course/get")
    public Result<CourseVO> getScheduleByStudentId(@RequestHeader("X-Student-Id") String studentId) {
        CourseVO vo = userCourseService.getScheduleByStudentId(studentId);
        return Result.success(vo);
    }

    @Operation(summary = "同步成绩数据（内部调用）")
    @PostMapping("/grade/sync")
    public Result<String> syncGradeData(@RequestBody GradeDTO gradeDTO) {
        userCourseService.saveOrUpdateGrades(gradeDTO);
        return Result.success("成绩同步入库成功");
    }

    @Operation(summary = "根据学号、学年、学期获取成绩（内部调用）")
    @GetMapping("/grade/get")
    public Result<List<GradeVO>> getGradesByStudentId(@RequestHeader("X-Student-Id") String studentId,
                                                      @RequestParam("academicYear") String academicYear,
                                                      @RequestParam("semester") String semester) {
        List<GradeVO> list = userCourseService.getGradesByStudentId(studentId, academicYear, semester);
        return Result.success(list);
    }

}