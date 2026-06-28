package com.campusassistant.remote.course.service.impl;

import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.client.CourseServiceClient;
import com.campusassistant.remote.course.pojo.RemoteCourseVO;
import com.campusassistant.remote.course.service.UserCourseService;
import com.campusassistant.utils.UserContextUtil;
import com.campusassistant.utils.redistool.CommonCacheService;
import com.campusassistant.utils.rediskey.CourseMixCacheKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCourseServiceImpl implements UserCourseService {

    private final CourseServiceClient courseServiceClient;
    private final CommonCacheService commonCacheService;
    private final CourseMixCacheKey courseMixCacheKey;

    @Override
    public RemoteCourseVO getScheduleWithCache() {
        String studentId = UserContextUtil.requireStudentId();
        return commonCacheService.getWithCache(
                courseMixCacheKey.getKey(studentId),
                RemoteCourseVO.class,      // 返回类型
                () -> {            // Lambda表达式：告诉它怎么查库并转换
                    Result<RemoteCourseVO> result = courseServiceClient.getSchedule(studentId);
                    return result.getData();
                }
        );
    }
}
