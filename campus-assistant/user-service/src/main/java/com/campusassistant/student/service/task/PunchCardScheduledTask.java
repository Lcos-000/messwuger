package com.campusassistant.student.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusassistant.student.common.PunchStatusEnum;
import com.campusassistant.remote.spider.service.SpiderService;
import com.campusassistant.student.mapper.UserMapper;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.service.impl.support.UserWriteSupport;
import com.campusassistant.utils.rediskey.UserPwdCacheKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PunchCardScheduledTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final SpiderService spiderService;
    private final UserPwdCacheKey userPwdCacheKey;
    private final UserMapper userMapper;
    private final UserWriteSupport userWriteSupport;

    private static final int BATCH_SIZE = 100;
    private static final long STAGGER_DELAY_MS = 200L;

    /**
     * 1. 每日重置任务：每天中午 12:00，将所有人的打卡状态重置为"未打卡" (0)
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void resetPunchStatusDaily() {
        String lockKey = "lock:scheduled:punchcard:reset";
        if (!tryLock(lockKey, 15)) return;

        try {
            log.info("开始执行每日打卡状态重置任务");
            userWriteSupport.resetAllPunchStatus();
            log.info("每日打卡状态重置完成");
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * 2. 轮询打卡任务：每晚 21:05 到 22:30，每隔 15 分钟执行一次
     */
    @Scheduled(cron = "0 5/15 21,22 * * ?")
    public void scheduledPunchCardLoop() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(22, 30))) {
            return;
        }

        String lockKey = "lock:scheduled:punchcard:loop";
        if (!tryLock(lockKey, 20)) return;

        try {
            log.info("自动打卡轮询任务开始执行 (当前时间: {})", now);
            // 只查 未打卡(0) 和 打卡失败(3)，跳过"打卡中(1)"避免重复触发
            List<Integer> retryStatuses = Arrays.asList(
                    PunchStatusEnum.NOT_PUNCHED.getCode(),
                    PunchStatusEnum.PUNCH_FAILED.getCode()
            );
            doPunchCardBatch(retryStatuses);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * 核心逻辑：基于 ID 游标的滚动分批查询触发打卡
     *   - 语义更清晰，直接表达"查哪些状态"
     *   - 消除了 isNotEquals 布尔参数带来的歧义
     *   不再是可外部注入的变量，保持现有安全性；若将来需动态配置，应改用参数绑定。
     */
    private void doPunchCardBatch(List<Integer> statusList) {
        long lastId = 0L;
        int batchCount = 1;

        while (true) {
            LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();

            lqw.in(UserEntity::getPunchStatus, statusList);
            lqw.gt(UserEntity::getId, lastId);
            lqw.orderByAsc(UserEntity::getId);
            lqw.last("LIMIT " + BATCH_SIZE);

            List<UserEntity> targetUsers = userMapper.selectList(lqw);

            if (targetUsers == null || targetUsers.isEmpty()) {
                log.info("本轮打卡任务处理完毕，共处理 {} 批次。", batchCount - 1);
                break;
            }

            log.info("打卡任务：正在处理第 {} 批次，共 {} 个用户", batchCount, targetUsers.size());

            for (UserEntity user : targetUsers) {
                try {
                    String password = stringRedisTemplate.opsForValue()
                            .get(userPwdCacheKey.getKey(user.getStudentId()));

                    if (password != null) {
                        spiderService.asyncStartPunchCard(user.getStudentId(), password);

                        Thread.sleep(STAGGER_DELAY_MS);
                    } else {
                        log.warn("打卡任务：学号 {} 密码缓存已过期，跳过", user.getStudentId());
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("打卡任务被中断，退出当前批次");
                    return;
                } catch (Exception e) {
                    log.error("打卡任务：处理学号 {} 异常，跳过", user.getStudentId(), e);
                } finally {
                    lastId = user.getId();
                }
            }

            batchCount++;
        }
    }

    /**
     * 分布式锁
     */
    private boolean tryLock(String lockKey, long minutes) {
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", minutes, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(locked)) {
            log.info("打卡任务已被其他实例执行，跳过: {}", lockKey);
            return false;
        }
        return true;
    }
}