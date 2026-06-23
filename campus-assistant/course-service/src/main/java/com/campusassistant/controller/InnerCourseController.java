// 路径：controller/InnerCourseController.java (对内部微服务暴露)
package com.campusassistant.controller;

import com.campusassistant.common.UserContext;
import com.campusassistant.pojo.CourseDTO;
import com.campusassistant.pojo.CourseVO;
import com.campusassistant.pojo.Result;
import com.campusassistant.service.UserCourseService;
import com.campusassistant.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inner/course")
@RequiredArgsConstructor
public class InnerCourseController {

    private final UserCourseService userCourseService;

    /**
     * 提供给 user-service 回调后，通过 OpenFeign 调用的内部写库接口
     */
    @PostMapping("/sync")
    public Result<String> syncScheduleData(@RequestBody CourseDTO courseDTO) {
        userCourseService.saveOrUpdateSchedule(courseDTO);
        return Result.success("课表同步入库成功");
    }

    @GetMapping("/get")
    public Result<CourseVO> getScheduleByStudentId(@RequestHeader("X-Student-Id") String studentId) {
        CourseVO vo = userCourseService.getScheduleByStudentId(studentId);
        return Result.success(vo);
    }

}