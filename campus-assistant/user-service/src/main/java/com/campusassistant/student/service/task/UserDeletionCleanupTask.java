package com.campusassistant.student.service.task;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.pojo.Result;
import com.campusassistant.personalization.service.AliyunOssService;
import com.campusassistant.remote.course.client.CourseServiceClient;
import com.campusassistant.remote.spider.common.service.SpiderService;
import com.campusassistant.student.mapper.UserDeletionTaskMapper;
import com.campusassistant.student.pojo.entity.UserDeletionTaskEntity;
import com.campusassistant.support.ScheduledLockSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletionCleanupTask {

    private static final String CLEANUP_LOCK_KEY = "lock:scheduled:user-deletion-cleanup";
    private static final int BATCH_SIZE = 20;
    private static final long LEASE_MINUTES = 10;

    private final UserDeletionTaskMapper taskMapper;
    private final CourseServiceClient courseServiceClient;
    private final SpiderService spiderService;
    private final AliyunOssService aliyunOssService;
    private final ScheduledLockSupport scheduledLockSupport;

    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000)
    public void cleanupDeletedUsers() {
        scheduledLockSupport.executeWithLock(
                CLEANUP_LOCK_KEY,
                2,
                TimeUnit.MINUTES,
                this::processPendingTasks
        );
    }

    private void processPendingTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<UserDeletionTaskEntity> tasks = taskMapper.claimTasks(
                now, now.plusMinutes(LEASE_MINUTES), BATCH_SIZE);
        for (UserDeletionTaskEntity task : tasks) {
            process(task);
        }
    }

    protected void process(UserDeletionTaskEntity task) {
        String leaseToken = task.getLeaseToken();
        try {
            renewLease(task, leaseToken);
            if (!spiderService.deleteSession(task.getStudentId())) {
                throw new IllegalStateException("删除 Spider 会话失败");
            }

            renewLease(task, leaseToken);
            Result<String> courseResult = courseServiceClient.deleteStudentData(task.getStudentId());
            if (courseResult == null
                    || !Objects.equals(courseResult.getCode(), ResultCodeEnum.SUCCESS.getCode())) {
                throw new IllegalStateException("删除 Course 数据失败");
            }

            renewLease(task, leaseToken);
            aliyunOssService.deleteByUrl(task.getCustomAvatar());
            aliyunOssService.deleteByUrl(task.getCustomBackground());
            aliyunOssService.deleteByUrl(task.getCustomWallpaper());

            if (taskMapper.markDone(task.getId(), leaseToken) == 0) {
                log.warn("用户注销清理任务租约已失效，跳过完成状态更新，studentId={}", task.getStudentId());
            }
        } catch (Exception e) {
            int retryCount = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
            long delaySeconds = Math.min(3600L, 1L << Math.min(retryCount, 11));
            String error = truncate(e.getMessage());
            if (taskMapper.markRetry(task.getId(), leaseToken, retryCount,
                    LocalDateTime.now().plusSeconds(delaySeconds), error) == 1) {
                log.error("用户注销后的资源清理失败，studentId={}，将在 {} 秒后重试",
                        task.getStudentId(), delaySeconds, e);
            } else {
                log.warn("用户注销清理任务租约已失效，跳过失败状态更新，studentId={}", task.getStudentId(), e);
            }
        }
    }

    private void renewLease(UserDeletionTaskEntity task, String leaseToken) {
        LocalDateTime leaseUntil = LocalDateTime.now().plusMinutes(LEASE_MINUTES);
        if (taskMapper.renewLease(task.getId(), leaseToken, leaseUntil) != 1) {
            throw new IllegalStateException("清理任务租约已失效");
        }
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "unknown cleanup error";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
