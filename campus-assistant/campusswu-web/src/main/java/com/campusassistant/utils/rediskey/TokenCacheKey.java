package com.campusassistant.utils.rediskey;

import org.springframework.stereotype.Component;

@Component
public class TokenCacheKey extends AbstractCacheKeyProvider<String>{
    private final String Token_Pre = "admin_token:";

    @Override
    public String getPrefix() {
        return Token_Pre;
    }
}
