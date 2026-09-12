package com.campusassistant.student.service.impl.support;

import com.campusassistant.key.TokenCacheKey;
import com.campusassistant.utils.rediskey.user.UserPersonalCacheKey;
import com.campusassistant.utils.rediskey.user.UserPwdCacheKey;
import com.campusassistant.utils.rediskey.user.UserStatusCacheKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCacheSupportTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private UserStatusCacheKey userStatusCacheKey;

    @Mock
    private UserPwdCacheKey userPwdCacheKey;

    @Mock
    private UserPersonalCacheKey userPersonalCacheKey;

    @Mock
    private TokenCacheKey tokenCacheKey;

    private UserCacheSupport support;

    @BeforeEach
    void setUp() {
        support = new UserCacheSupport(
                redisTemplate,
                userStatusCacheKey,
                userPwdCacheKey,
                userPersonalCacheKey,
                tokenCacheKey
        );
        when(userPwdCacheKey.getKey("2025001")).thenReturn("user:pwd:2025001");
        when(userStatusCacheKey.getKey("2025001")).thenReturn("user:status:2025001");
        when(userPersonalCacheKey.getKey("2025001")).thenReturn("user:personal:2025001");
    }

    @Test
    void evictLoginSessionAndUserCaches_normalizesBearerTokenBeforeDeleting() {
        when(tokenCacheKey.getKey("jwt-token")).thenReturn("token:jwt-token");

        support.evictLoginSessionAndUserCaches("2025001", "Bearer jwt-token");

        verify(tokenCacheKey).getKey("jwt-token");
        verify(redisTemplate).delete("token:jwt-token");
        verify(redisTemplate).delete("user:pwd:2025001");
        verify(redisTemplate).delete("user:status:2025001");
        verify(redisTemplate).delete("user:personal:2025001");
    }

    @Test
    void evictLoginSessionAndUserCaches_skipsTokenKeyWhenTokenIsBlank() {
        support.evictLoginSessionAndUserCaches("2025001", " ");

        verify(tokenCacheKey, never()).getKey(org.mockito.ArgumentMatchers.anyString());
        verify(redisTemplate).delete("user:pwd:2025001");
        verify(redisTemplate).delete("user:status:2025001");
        verify(redisTemplate).delete("user:personal:2025001");
    }
}
