package com.campusassistant.remote.spider.emptyclassroom.support;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomCallbackDTO;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmptyClassCallbackCheckSupport {

    private final UserReadSupport userReadSupport;

    public String validateEmptyClassroomCallbackForCache(EmptyClassroomCallbackDTO callbackDTO) {
        if (callbackDTO == null) {
            return "callbackDTO为空";
        }
        if (callbackDTO.getAcademicYear() == null || callbackDTO.getAcademicYear().isBlank()) {
            return "academicYear为空";
        }
        if (callbackDTO.getSemester() == null || !callbackDTO.getSemester().matches("^(3|6|12)$")) {
            return "semester非法";
        }
        if (callbackDTO.getDayOfWeek() == null || !callbackDTO.getDayOfWeek().matches("^[1-7]$")) {
            return "dayOfWeek非法";
        }
        if (callbackDTO.getPeriodsMask() == null || callbackDTO.getPeriodsMask().isBlank()) {
            return "periodsMask为空";
        }
        if (callbackDTO.getWeeksMask() == null || callbackDTO.getWeeksMask().isBlank()) {
            return "weeksMask为空";
        }
        return null;
    }

    public void checkEmptyClassroomCallbackUser(EmptyClassroomCallbackDTO callbackDTO) {
        String studentId = callbackDTO.getStudentId();
        if (studentId == null || studentId.isBlank()) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "studentId为空");
        }
        if (userReadSupport.findEntityByStudentId(studentId) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(), "未找到对应用户");
        }
    }

}
