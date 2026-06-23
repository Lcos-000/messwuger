package com.campusassistant.student.service;




import com.campusassistant.student.pojo.UserVO;

public interface CurrentUserService {

    void self_unsubscribe();

    UserVO getBasicById(Long id);

    UserVO getBasicByStudentId(String studentId);

    void refreshData();

    UserVO getPersonalByStudentId(String studentId);
}
