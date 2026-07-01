package com.campusassistant.key;

import org.springframework.stereotype.Component;

@Component
public class TokenCacheKey extends AbstractCacheKeyProvider<String>{
    private final String Token_Prefix = "token:";

    @Override
    public String getPrefix() {
        return Token_Prefix;
    }
}
