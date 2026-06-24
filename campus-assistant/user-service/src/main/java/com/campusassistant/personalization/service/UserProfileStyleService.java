package com.campusassistant.personalization.service;

import com.campusassistant.personalization.pojo.UserProfileStyleUpdateDTO;
import com.campusassistant.personalization.pojo.vo.UserProfileStyleVO;

public interface UserProfileStyleService {

    // 根据学号查询用户个性化样式
    UserProfileStyleVO getByStudentId();

    // 初始化用户个性化样式，注册成功后调用
    void initByStudentId();

    // 更新用户个性化样式
    void updateByStudentId(UserProfileStyleUpdateDTO dto);
}
