package com.campusassistant.utils.rediskey;

import org.springframework.stereotype.Component;

@Component
public class GradeCacheKey {
    private static final String GRADE_KEY_PREFIX = "grade:";

    public String getKey(String studentId, String academicYear, String semester) {
        return GRADE_KEY_PREFIX + studentId + ":" + academicYear + ":" + semester;
    }
}