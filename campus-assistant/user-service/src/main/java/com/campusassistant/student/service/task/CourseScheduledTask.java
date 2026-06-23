package com.campusassistant.student.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campusassistant.remote.spider.common.SyncStatusEnum;
import com.campusassistant.remote.spider.service.SpiderService;
import com.campusassistant.student.mapper.UserMapper;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.utils.rediskey.UserPwdCacheKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseScheduledTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final SpiderService spiderService;
    private final UserPwdCacheKey userPwdCacheKey;
    private final UserMapper userMapper;

    /**
     * 主任务：每天凌晨3点全量同步
     * scan Redis中所有有密码缓存的用户，逐批触发爬取
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledCrawlDaily() {
        String lockKey = "lock:scheduled:crawl:daily";
        if (!tryLock(lockKey)) return;

        log.info("每日全量同步任务开始执行");
        try {
            doScanCrawl();
        } finally {
            stringRedisTemplate.delete(lockKey);
            log.info("每日全量同步任务执行完毕，锁已释放");
        }
    }

    /**
     * 补偿任务：每天凌晨5点重试非SUCCESS状态的用户
     * 包括同步中（可能上次卡住）和失败状态
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void scheduledCrawlCompensate() {
        String lockKey = "lock:scheduled:crawl:compensate";
        if (!tryLock(lockKey)) return;

        log.info("补偿同步任务开始执行");
        try {
            doFailedCrawl();
        } finally {
            stringRedisTemplate.delete(lockKey);
            log.info("补偿同步任务执行完毕，锁已释放");
        }
    }

    /**
     * 全量扫描：scan Redis密码key，有密码就触发爬取
     */
    private void doScanCrawl() {
        String pattern = userPwdCacheKey.getPrefix() + "*";
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                try {
                    String key = cursor.next();
                    String password = stringRedisTemplate.opsForValue().get(key);

                    if (password != null) {
                        String studentId = key.replace(userPwdCacheKey.getPrefix(), "");
                        spiderService.asyncStartFullCrawl(studentId, password);
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    log.warn("全量同步：线程中断，跳过当前用户继续执行: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("全量同步：处理单个用户异常，跳过继续执行", e);
                }
            }
        } catch (Exception e) {
            log.error("全量同步：游标创建失败，任务终止", e);
        }
    }

    /**
     * 补偿扫描：查非SUCCESS状态用户重新触发爬取
     * 同步中状态说明上次任务可能没有收到回调，也需要重试
     */
    private void doFailedCrawl() {
        try {
            LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
            lqw.ne(UserEntity::getSyncStatus, SyncStatusEnum.SYNCING_SUCCESS.getCode());

            List<UserEntity> failedUsers = userMapper.selectList(lqw);

            if (failedUsers == null || failedUsers.isEmpty()) {
                log.info("补偿任务：无需重试的用户，跳过");
                return;
            }

            log.info("补偿任务：发现 {} 个非成功状态用户，开始重试", failedUsers.size());

            for (UserEntity user : failedUsers) {
                try {
                    String password = stringRedisTemplate.opsForValue()
                            .get(userPwdCacheKey.getKey(user.getStudentId()));

                    if (password == null) {
                        log.warn("补偿任务：学号 {} 密码缓存已过期，等待用户下次登录", user.getStudentId());
                        continue;
                    }

                    spiderService.asyncStartFullCrawl(user.getStudentId(), password);
                    Thread.sleep(200);

                } catch (InterruptedException e) {
                    log.warn("补偿任务：线程中断，跳过当前用户: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("补偿任务：处理学号 {} 异常，跳过", user.getStudentId(), e);
                }
            }
        } catch (Exception e) {
            log.error("补偿任务：查询用户异常，任务终止", e);
        }
    }


    @Scheduled(cron = "0 0 6 * * ?")
    public void scheduledCrawlReview() {
        String lockKey = "lock:scheduled:crawl:review";
        if (!tryLock(lockKey)) return;

        log.info("状态审查任务开始执行");
        try {
            LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
            lqw.ne(UserEntity::getSyncStatus, SyncStatusEnum.SYNCING_SUCCESS.getCode());

            List<UserEntity> pendingUsers = userMapper.selectList(lqw);

            if (pendingUsers == null || pendingUsers.isEmpty()) {
                log.info("状态审查任务：无异常状态用户，跳过");
                return;
            }

            log.info("状态审查任务：发现 {} 个未成功用户，标记为失败", pendingUsers.size());

            List<String> studentIds = pendingUsers.stream()
                    .map(UserEntity::getStudentId)
                    .collect(Collectors.toList());

            // 批量更新为失败状态
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(UserEntity::getStudentId, studentIds)
                    .set(UserEntity::getSyncStatus, SyncStatusEnum.SYNCING_FAILED.getCode());
            userMapper.update(null, updateWrapper);

            log.info("状态审查任务：已将 {} 个用户状态标记为失败", studentIds.size());

        } catch (Exception e) {
            log.error("状态审查任务异常", e);
        } finally {
            stringRedisTemplate.delete(lockKey);
            log.info("状态审查任务执行完毕，锁已释放");
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