package com.campusassistant.remote.course.service;

import com.campusassistant.remote.course.pojo.schedule.RemoteCourseVO;

public interface UserCourseService {

    RemoteCourseVO getScheduleWithCache();
}
