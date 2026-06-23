package com.campusassistant.student.service;

import com.campusassistant.student.pojo.dto.LoginDTO;
import com.campusassistant.student.pojo.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    void register(UserDTO UserDTO);

    String login(LoginDTO loginDTO);

    void logout(HttpServletRequest request);

}
