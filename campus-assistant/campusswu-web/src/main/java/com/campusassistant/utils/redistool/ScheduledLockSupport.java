package com.campusassistant.utils.redistool;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ScheduledLockSupport {

    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end",
            Long.class
    );

    public boolean executeWithLock(String lockKey, long timeout, TimeUnit timeUnit, Runnable action) {
        String lockValue = tryLock(lockKey, timeout, timeUnit);
        if (lockValue == null) {
            return false;
        }

        try {
            action.run();
            return true;
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    private String tryLock(String lockKey, long timeout, TimeUnit timeUnit) {
        String lockValue = UUID.randomUUID().toString().replace("-", "");
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeout, timeUnit);
        return Boolean.TRUE.equals(locked) ? lockValue : null;
    }

    private void unlock(String lockKey, String lockValue) {
        if (lockValue == null || lockValue.isEmpty()) {
            return;
        }

        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(lockKey),
                lockValue
        );
    }
}