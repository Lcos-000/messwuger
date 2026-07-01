package com.campusassistant.admin.service;

import com.campusassistant.student.pojo.dto.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminAuthService {


    String login(LoginDTO loginDTO);

    void logout(HttpServletRequest request);

}
