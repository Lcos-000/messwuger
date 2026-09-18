package com.campusassistant.student.service.impl.support;

import com.campusassistant.utils.rediskey.user.UserPersonalCacheKey;
import com.campusassistant.utils.rediskey.user.UserPwdCacheKey;
import com.campusassistant.utils.rediskey.user.UserStatusCacheKey;
import com.campusassistant.key.TokenCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.campusassistant.utils.TokenTool.normalizeToken;

@Service
@RequiredArgsConstructor
public class UserCacheSupport {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserStatusCacheKey userStatusCacheKey;
    private final UserPwdCacheKey userPwdCacheKey;
    private final UserPersonalCacheKey userPersonalCacheKey;
    private final TokenCacheKey tokenCacheKey;

    public void evictLoginSessionAndUserCaches(String studentId, String token){
        String normalizedToken = normalizeToken(token);
        if (normalizedToken != null) {
            stringRedisTemplate.delete(tokenCacheKey.getKey(normalizedToken));
        }
        stringRedisTemplate.delete(userPwdCacheKey.getKey(studentId));
        stringRedisTemplate.delete(userStatusCacheKey.getKey(studentId));
        stringRedisTemplate.delete(userPersonalCacheKey.getKey(studentId));
    }



}
