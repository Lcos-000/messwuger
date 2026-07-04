package com.campusassistant.remote.course.client;


import com.campusassistant.remote.config.FeignConfig;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.pojo.RemoteGradeBatchDTO;
import com.campusassistant.remote.course.pojo.RemoteGradeVO;
import com.campusassistant.remote.course.pojo.schedule.RemoteCourseDTO;
import com.campusassistant.remote.course.pojo.schedule.RemoteCourseVO;
import com.campusassistant.remote.exception.fallback.UserCourseFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "user-course-service-client",
        name = "campus-course-service",
//        url = "http://localhost:9000",
        configuration = FeignConfig.class,
        fallbackFactory = UserCourseFallbackFactory.class)
public interface CourseServiceClient {

    // 调底层兜底查课表接口
    @GetMapping("inner/course/get")
    Result<RemoteCourseVO> getSchedule(@RequestHeader("X-Student-Id") String studentId);

    // 调底层同步入库接口
    @PostMapping("/inner/course/sync")
    Result<String> syncScheduleData(@RequestBody RemoteCourseDTO remoteCourseDTO);

    // 更新成绩数据入库接口
    @PostMapping("/inner/grade/sync")
    Result<String> syncGradeData(@RequestBody RemoteGradeBatchDTO remoteGradeBatchDTO);

    // 查询成绩数据
    @GetMapping("/inner/grade/get")
    Result<List<RemoteGradeVO>> getGrades(@RequestHeader("X-Student-Id") String studentId,
                                          @RequestParam("academicYear") String academicYear,
                                          @RequestParam("semester") String semester);


}

