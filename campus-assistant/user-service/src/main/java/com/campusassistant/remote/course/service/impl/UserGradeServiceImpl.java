package com.campusassistant.remote.course.service.impl;

import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.client.CourseServiceClient;
import com.campusassistant.remote.course.pojo.RemoteGradeVO;
import com.campusassistant.remote.course.service.UserGradeService;
import com.campusassistant.service.CommonCacheService;
import com.campusassistant.utils.UserContextUtil;
import com.campusassistant.utils.rediskey.GradeCacheKey;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserGradeServiceImpl implements UserGradeService {

    private final CourseServiceClient courseServiceClient;
    private final CommonCacheService commonCacheService;
    private final GradeCacheKey gradeCacheKey;

    @Override
    public List<RemoteGradeVO> getGradesWithCache(String academicYear, String semester) {
        String studentId = UserContextUtil.requireStudentId();

        return commonCacheService.getWithCache(
                gradeCacheKey.getKey(studentId, academicYear, semester),
                new TypeReference<>() {
                },
                () -> {
                    Result<List<RemoteGradeVO>> result = courseServiceClient.getGrades(studentId, academicYear, semester);
                    return result.getData();
                }
        );
    }
}