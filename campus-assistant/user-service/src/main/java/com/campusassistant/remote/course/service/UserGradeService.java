package com.campusassistant.remote.course.service;

import com.campusassistant.remote.course.pojo.RemoteGradeVO;

import java.util.List;

public interface UserGradeService {

    List<RemoteGradeVO> getGradesWithCache(String academicYear, String semester);
}