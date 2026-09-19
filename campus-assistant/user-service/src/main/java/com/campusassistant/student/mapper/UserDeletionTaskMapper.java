package com.campusassistant.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campusassistant.student.pojo.entity.UserDeletionTaskEntity;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper
public interface UserDeletionTaskMapper extends BaseMapper<UserDeletionTaskEntity> {

    String STATUS_PENDING = "PENDING";
    String STATUS_PROCESSING = "PROCESSING";
    String STATUS_FAILED = "FAILED";

    default boolean existsPendingByStudentId(String studentId) {
        return selectCount(new LambdaQueryWrapper<UserDeletionTaskEntity>()
                .eq(UserDeletionTaskEntity::getStudentId, studentId)
                .in(UserDeletionTaskEntity::getStatus, STATUS_PENDING, STATUS_PROCESSING)) > 0;
    }

    /**
     * Atomically claims eligible tasks. The conditional update prevents two
     * service instances from processing the same task at the same time.
     */
    default List<UserDeletionTaskEntity> claimTasks(LocalDateTime now,
                                                    LocalDateTime leaseUntil,
                                                    int limit) {
        List<UserDeletionTaskEntity> candidates = selectList(
                new LambdaQueryWrapper<UserDeletionTaskEntity>()
                        .and(wrapper -> wrapper
                                .eq(UserDeletionTaskEntity::getStatus, STATUS_PENDING)
                                .le(UserDeletionTaskEntity::getNextRetryAt, now)
                                .or()
                                .eq(UserDeletionTaskEntity::getStatus, STATUS_PROCESSING)
                                .le(UserDeletionTaskEntity::getLeaseUntil, now))
                        .orderByAsc(UserDeletionTaskEntity::getId)
                        .last("LIMIT " + limit)
        );

        List<UserDeletionTaskEntity> claimed = new ArrayList<>();
        for (UserDeletionTaskEntity task : candidates) {
            String leaseToken = UUID.randomUUID().toString().replace("-", "");
            LambdaUpdateWrapper<UserDeletionTaskEntity> updateWrapper =
                    new LambdaUpdateWrapper<UserDeletionTaskEntity>()
                            .eq(UserDeletionTaskEntity::getId, task.getId())
                            .and(wrapper -> wrapper
                                    .eq(UserDeletionTaskEntity::getStatus, STATUS_PENDING)
                                    .le(UserDeletionTaskEntity::getNextRetryAt, now)
                                    .or()
                                    .eq(UserDeletionTaskEntity::getStatus, STATUS_PROCESSING)
                                    .le(UserDeletionTaskEntity::getLeaseUntil, now))
                            .set(UserDeletionTaskEntity::getStatus, STATUS_PROCESSING)
                            .set(UserDeletionTaskEntity::getLeaseToken, leaseToken)
                            .set(UserDeletionTaskEntity::getLeaseUntil, leaseUntil);
            if (update(null, updateWrapper) == 1) {
                task.setStatus(STATUS_PROCESSING);
                task.setLeaseToken(leaseToken);
                task.setLeaseUntil(leaseUntil);
                claimed.add(task);
            }
        }
        return claimed;
    }

    default int renewLease(Long taskId, String leaseToken, LocalDateTime leaseUntil) {
        return update(null, new LambdaUpdateWrapper<UserDeletionTaskEntity>()
                .eq(UserDeletionTaskEntity::getId, taskId)
                .eq(UserDeletionTaskEntity::getStatus, STATUS_PROCESSING)
                .eq(UserDeletionTaskEntity::getLeaseToken, leaseToken)
                .set(UserDeletionTaskEntity::getLeaseUntil, leaseUntil));
    }

    default int markDone(Long taskId, String leaseToken) {
        return update(null, new LambdaUpdateWrapper<UserDeletionTaskEntity>()
                .eq(UserDeletionTaskEntity::getId, taskId)
                .eq(UserDeletionTaskEntity::getStatus, STATUS_PROCESSING)
                .eq(UserDeletionTaskEntity::getLeaseToken, leaseToken)
                .set(UserDeletionTaskEntity::getStatus, "DONE")
                .set(UserDeletionTaskEntity::getLeaseToken, null)
                .set(UserDeletionTaskEntity::getLeaseUntil, null)
                .set(UserDeletionTaskEntity::getLastError, null));
    }

    default int markRetry(Long taskId,
                          String leaseToken,
                          int retryCount,
                          LocalDateTime nextRetryAt,
                          String lastError) {
        return update(null, new LambdaUpdateWrapper<UserDeletionTaskEntity>()
                .eq(UserDeletionTaskEntity::getId, taskId)
                .eq(UserDeletionTaskEntity::getStatus, STATUS_PROCESSING)
                .eq(UserDeletionTaskEntity::getLeaseToken, leaseToken)
                .set(UserDeletionTaskEntity::getStatus, STATUS_PENDING)
                .set(UserDeletionTaskEntity::getRetryCount, retryCount)
                .set(UserDeletionTaskEntity::getNextRetryAt, nextRetryAt)
                .set(UserDeletionTaskEntity::getLeaseToken, null)
                .set(UserDeletionTaskEntity::getLeaseUntil, null)
                .set(UserDeletionTaskEntity::getLastError, lastError));
    }

    default int markFailed(Long taskId,
                           String leaseToken,
                           int retryCount,
                           String lastError) {
        return update(null, new LambdaUpdateWrapper<UserDeletionTaskEntity>()
                .eq(UserDeletionTaskEntity::getId, taskId)
                .eq(UserDeletionTaskEntity::getStatus, STATUS_PROCESSING)
                .eq(UserDeletionTaskEntity::getLeaseToken, leaseToken)
                .set(UserDeletionTaskEntity::getStatus, STATUS_FAILED)
                .set(UserDeletionTaskEntity::getRetryCount, retryCount)
                .set(UserDeletionTaskEntity::getLeaseToken, null)
                .set(UserDeletionTaskEntity::getLeaseUntil, null)
                .set(UserDeletionTaskEntity::getLastError, lastError));
    }
}
