package com.campusassistant.utils.rediskey;

import org.springframework.stereotype.Component;

@Component
public class EmptyClassroomCacheKey {

    private static final String RESULT_PREFIX = "empty_classroom:result:";
    private static final String STATUS_PREFIX = "empty_classroom:status:";

    public String getResultKey(String fingerprint) {
        return RESULT_PREFIX + fingerprint;
    }

    public String getStatusKey(String fingerprint) {
        return STATUS_PREFIX + fingerprint;
    }
}