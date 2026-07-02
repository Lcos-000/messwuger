package com.campusassistant.utils.rediskey;

import com.campusassistant.key.AbstractCacheKeyProvider;
import org.springframework.stereotype.Component;

@Component
public class UserStatusCacheKey extends AbstractCacheKeyProvider<String> {

    private static final String USER_STATUS_PREFIX = "user:status:";

    @Override
    public String getPrefix() {
        return USER_STATUS_PREFIX;
    }
}