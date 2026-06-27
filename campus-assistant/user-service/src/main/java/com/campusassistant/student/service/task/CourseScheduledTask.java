package com.campusassistant.student.service.task;

import com.campusassistant.student.service.impl.support.UserWriteSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.campusassistant.common.SyncStatusEnum.NOT_SYNCED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseScheduledTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserWriteSupport userWriteSupport;

    /**
     * 每周日凌晨重置所有用户的同步状态为未同步
     * 下次用户登录时会自动触发爬虫同步最新课表
     */
    @Scheduled(cron = "0 0 23 ? * SUN")
    public void resetSyncStatusWeekly() {
        String lockKey = "lock:scheduled:reset:weekly";
        if (!tryLock(lockKey)) return;

        log.info("每周同步状态重置任务开始执行");
        try {
            userWriteSupport.resetAllSyncStatus(NOT_SYNCED.getCode());
            log.info("所有用户的同步状态已重置为未同步");
        } catch (Exception e) {
            log.error("每周同步状态重置任务异常", e);
        } finally {
            stringRedisTemplate.delete(lockKey);
            log.info("每周同步状态重置任务执行完毕，锁已释放");
        }
    }

    /**
     * 尝试获取分布式锁
     */
    private boolean tryLock(String lockKey) {
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(locked)) {
            log.info("任务已被其他实例执行，跳过: {}", lockKey);
            return false;
        }
        return true;
    }
}