package com.campusassistant.admin.service;

import com.campusassistant.admin.pojo.vo.AdminResourceVO;
import com.campusassistant.student.pojo.dto.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminResourceService {

    AdminResourceVO getResourceList();
}