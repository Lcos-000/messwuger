package com.campusassistant.utils.rediskey;

import com.campusassistant.utils.redistool.rediskey.AbstractCacheKeyProvider;
import org.springframework.stereotype.Component;

@Component
public class CourseMixCacheKey extends AbstractCacheKeyProvider<String> {

    // 定义常量
    private static final String COURSE_PREFIX = "course:info:id:";

    @Override
    public String getPrefix() {
        return COURSE_PREFIX;
    }
}
