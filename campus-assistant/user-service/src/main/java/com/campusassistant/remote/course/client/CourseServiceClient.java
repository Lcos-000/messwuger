package com.campusassistant.remote.course.client;


import com.campusassistant.config.FeignConfig;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.pojo.RemoteCourseDTO;
import com.campusassistant.remote.course.pojo.RemoteCourseVO;
import com.campusassistant.remote.exception.fallback.UserCourseFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "campus-course-service",
        url = "http://localhost:9000",
        configuration = FeignConfig.class,
        fallbackFactory = UserCourseFallbackFactory.class)
public interface CourseServiceClient {

    // 调底层兜底查课表接口
    @GetMapping("inner/course/get")
    Result<RemoteCourseVO> getSchedule(@RequestHeader("X-Student-Id") String studentId);

    // 调底层同步入库接口
    @PostMapping("/inner/course/sync")
    Result<String> syncScheduleData(@RequestBody RemoteCourseDTO remoteCourseDTO);
}

