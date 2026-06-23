package com.campusassistant.controller;

import com.campusassistant.pojo.CourseVO;
import com.campusassistant.pojo.Result;
import com.campusassistant.service.UserCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class FrontCourseController {

    private final UserCourseService userCourseService;

    // 前端查询指定学期课表
    @GetMapping("/get")
    public Result<CourseVO> getSchedule() {
        CourseVO vo = userCourseService.getSchedule();
        return Result.success(vo);
    }

    // 前端打开小程序主页，默认查询最新课表
    @GetMapping("/current")
    public Result<CourseVO> getCurrentSchedule() {
        CourseVO vo = userCourseService.getCurrentSchedule();
        return Result.success(vo);
    }
}
