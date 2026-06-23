package com.campusassistant.student.service;

import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;

public interface CurrentUserService {

    void self_unsubscribe();

    UserStatusVO getStatusByStudentId(String studentId);

    void refreshData();

    PersonalInfoVO getPersonalByStudentId(String studentId);
}
