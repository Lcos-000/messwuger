// 路径：user-service/src/main/java/cloudstructuretemplate/controller/CourseRemoteController.java
package com.campusassistant.remote.course.controller;

import com.campusassistant.common.UserContext;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.pojo.RemoteCourseVO;
import com.campusassistant.remote.course.service.UserCourseService;
import com.campusassistant.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user/schedule")
@RequiredArgsConstructor
public class CourseRemoteController {

    private final UserCourseService userCourseService;

    @GetMapping("/get")
    public Result<RemoteCourseVO> getScheduleWithCache() {
        UserContext userContext = ThreadLocalUtil.get();
        RemoteCourseVO remoteCourseVO = userCourseService.getScheduleWithCache(userContext.getStudentId());
        return Result.success(remoteCourseVO);
    }
}
