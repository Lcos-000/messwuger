package com.campusassistant.student.service;

import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;

public interface CurrentUserService {

    void self_unsubscribe();

    UserStatusVO getStatusByStudentId();

    void refreshData();

    PersonalInfoVO getPersonalByStudentId();

    void updateAutoPunchEnabled(Integer enabled);
}
