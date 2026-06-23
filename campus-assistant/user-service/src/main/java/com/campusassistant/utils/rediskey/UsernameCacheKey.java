package com.campusassistant.utils.rediskey;

import com.campusassistant.utils.redistool.rediskey.AbstractCacheKeyProvider;
import org.springframework.stereotype.Component;

@Component
public class UsernameCacheKey extends AbstractCacheKeyProvider<String> {

    private static final String USER_NAME_PREFIX = "user:info:name:";

    @Override
    public String getPrefix() {
        return USER_NAME_PREFIX;
    }


}
