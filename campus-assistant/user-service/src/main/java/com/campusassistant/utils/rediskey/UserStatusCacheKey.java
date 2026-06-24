package com.campusassistant.utils.rediskey;

import com.campusassistant.utils.redistool.rediskey.AbstractCacheKeyProvider;
import org.springframework.stereotype.Component;

@Component
public class UserStatusCacheKey extends AbstractCacheKeyProvider<String> {

    private static final String USER_STATUS_PREFIX = "user:status:";

    @Override
    public String getPrefix() {
        return USER_STATUS_PREFIX;
    }
}