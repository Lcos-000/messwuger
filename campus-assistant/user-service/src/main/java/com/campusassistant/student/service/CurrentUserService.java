package com.campusassistant.student.service;

import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.pojo.RemoteGradeVO;
import com.campusassistant.student.pojo.dto.GradesQueryDTO;
import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.remote.spider.sync.pojo.vo.PersonalInfoVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CurrentUserService {

    void self_unsubscribe(HttpServletRequest request);

    UserStatusVO getStatusByStudentId();

    void refreshData();

    PersonalInfoVO getPersonalByStudentId();

    void updateAutoPunchEnabled(Integer enabled);

    Result<?> submitGradesTask(GradesQueryDTO dto);

    List<RemoteGradeVO> getGrades(String academicYear, String semester);

}
