package com.campusassistant.student.service.task;

import com.campusassistant.student.service.impl.support.UserWriteSupport;
import com.campusassistant.utils.redistool.ScheduledLockSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.campusassistant.student.code.SyncStatusEnum.NOT_SYNCED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseScheduledTask {

    private final UserWriteSupport userWriteSupport;
    private final ScheduledLockSupport scheduledLockSupport;

    private static final String COURSE_LOCK_KEY = "lock:scheduled:reset:weekly";
    private static final int COURSE_LOCK_TIMEOUT = 10;

    /**
     * 每周日凌晨重置所有用户的同步状态为未同步
     * 下次用户登录时会自动触发爬虫同步最新课表
     */
    @Scheduled(cron = "0 0 23 ? * SUN")
    public void resetSyncStatusWeekly() {
        boolean executed = scheduledLockSupport.executeWithLock(
                COURSE_LOCK_KEY,
                COURSE_LOCK_TIMEOUT,
                TimeUnit.MINUTES,
                () -> {
                    log.info("每周同步状态重置任务开始执行");
                    try {
                        userWriteSupport.resetAllSyncStatus(NOT_SYNCED.getCode());
                        log.info("所有用户的同步状态已重置为未同步");
                    } catch (Exception e) {
                        log.error("每周同步状态重置任务异常", e);
                    } finally {
                        log.info("每周同步状态重置任务执行完毕");
                    }
                }
        );

        if (!executed) {
            log.info("任务已被其他实例执行，跳过: {}", COURSE_LOCK_KEY);
        }
    }
}