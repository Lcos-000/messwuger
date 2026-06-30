package com.campusassistant.student.service.impl.support;

import com.campusassistant.utils.rediskey.UserPersonalCacheKey;
import com.campusassistant.utils.rediskey.UserPwdCacheKey;
import com.campusassistant.utils.rediskey.UserStatusCacheKey;
import com.campusassistant.utils.redistool.rediskey.TokenCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheSupport {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserStatusCacheKey userStatusCacheKey;
    private final UserPwdCacheKey userPwdCacheKey;
    private final UserPersonalCacheKey userPersonalCacheKey;
    private final TokenCacheKey tokenCacheKey;

    public void evictByStudentId(String studentId) {
        stringRedisTemplate.delete(userStatusCacheKey.getKey(studentId));
        stringRedisTemplate.delete(userPwdCacheKey.getKey(studentId));
        stringRedisTemplate.delete(userPersonalCacheKey.getKey(studentId));
    }

    public void evictLoginContext(String studentId, String token){
        stringRedisTemplate.delete(tokenCacheKey.getKey(token));
        stringRedisTemplate.delete(userPwdCacheKey.getKey(studentId));
        stringRedisTemplate.delete(userStatusCacheKey.getKey(studentId));
        stringRedisTemplate.delete(userPersonalCacheKey.getKey(studentId));
    }

}
