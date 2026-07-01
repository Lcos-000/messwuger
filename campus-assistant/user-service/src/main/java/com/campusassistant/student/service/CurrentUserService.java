package com.campusassistant.student.service;

import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;
import jakarta.servlet.http.HttpServletRequest;

public interface CurrentUserService {

    void self_unsubscribe(HttpServletRequest request);

    UserStatusVO getStatusByStudentId();

    void refreshData();

    PersonalInfoVO getPersonalByStudentId();

    void updateAutoPunchEnabled(Integer enabled);
}
