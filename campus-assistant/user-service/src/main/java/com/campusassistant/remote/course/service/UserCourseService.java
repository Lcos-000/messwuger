package com.campusassistant.remote.course.service;

import com.campusassistant.remote.course.pojo.RemoteCourseVO;

public interface UserCourseService {

    RemoteCourseVO getScheduleWithCache();
}
