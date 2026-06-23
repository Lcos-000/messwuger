package com.campusassistant.utils.rediskey;

import com.campusassistant.utils.redistool.rediskey.AbstractCacheKeyProvider;
import org.springframework.stereotype.Component;

@Component
public class StudentIdCacheKey extends AbstractCacheKeyProvider<String> {

    private static final String STUDENT_ID_PREFIX = "user:info:stu:";

    @Override
    public String getPrefix() {
        return STUDENT_ID_PREFIX;
    }
}
