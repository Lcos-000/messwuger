package com.campusassistant.student.service.impl.support;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.student.code.PunchStatusEnum;
import com.campusassistant.student.mapper.UserMapper;
import com.campusassistant.student.pojo.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.campusassistant.student.code.UserEnteringEnum.USER_ALREADY_EXISTS;
import static com.campusassistant.student.code.SyncStatusEnum.NOT_SYNCED;
import static com.campusassistant.student.code.PunchStatusEnum.AUTO_PUNCH_ENABLED;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWriteSupport {

    private final UserMapper userMapper;
    private final UserReadSupport userReadSupport;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void addUser(UserEntity userEntity) {
        if (userReadSupport.findEntityByStudentId(userEntity.getStudentId()) != null) {
            throw new BusinessException(USER_ALREADY_EXISTS);
        }
        String hash = passwordEncoder.encode(userEntity.getPassword());
        userEntity.setPassword(hash);
        userEntity.setSyncStatus(NOT_SYNCED.getCode());
        userEntity.setAutoPunchEnabled(AUTO_PUNCH_ENABLED.getCode());
        try {
            userMapper.insert(userEntity);
        } catch (DuplicateKeyException e) {
            // 兜底处理：防止并发导致的数据库唯一键冲突
            throw new BusinessException(USER_ALREADY_EXISTS);
        }
    }

    public int deleteUserById(Long id) {
        return userMapper.deleteById(id);
    }


    public void updateSyncStatus(String studentId, Integer status) {
        try {
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserEntity::getStudentId, studentId);

            UserEntity updateUser = new UserEntity();
            updateUser.setSyncStatus(status);

            int rows = userMapper.update(updateUser, updateWrapper);

            if (rows == 0) {
                log.warn("更新同步状态失败，未找到学号为 {} 的用户", studentId);
            } else {
                log.info("用户 {} 的同步状态已更新为: {}", studentId, status);
            }
        } catch (Exception e) {
            // 捕获异常防止主流程崩溃，但记录日志
            log.error("数据库更新同步状态异常, studentId: {}, status: {}", studentId, status, e);
        }
    }

    public void resetAllSyncStatus(Integer status) {
        try {
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.gt(UserEntity::getId, 0);
            updateWrapper.set(UserEntity::getSyncStatus, status);
            int rows = userMapper.update(null, updateWrapper);
            log.info("已批量重置 {} 个用户的同步状态为: {}", rows, status);
        } catch (Exception e) {
            log.error("批量重置同步状态异常", e);
        }
    }

    public void updatePunchStatus(String studentId, Integer status) {
        try {
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserEntity::getStudentId, studentId);

            UserEntity updateUser = new UserEntity();
            updateUser.setPunchStatus(status);

            int rows = userMapper.update(updateUser, updateWrapper);
            log.info("用户 {} 的打卡状态已更新为: {}", studentId, status);
        } catch (Exception e) {
            log.error("更新打卡状态异常, studentId: {}, status: {}", studentId, status, e);
        }
    }

    public void resetAllPunchStatus() {
        try {
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.gt(UserEntity::getId, 0);
            updateWrapper.set(UserEntity::getPunchStatus, PunchStatusEnum.NOT_PUNCHED.getCode());
            int rows = userMapper.update(null, updateWrapper);
            log.info("已批量重置 {} 个用户的打卡状态为未打卡", rows);
        } catch (Exception e) {
            log.error("批量重置打卡状态异常", e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }

    public void updateAutoPunchEnabled(String studentId, Integer enabled) {
        try {
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserEntity::getStudentId, studentId);

            UserEntity updateUser = new UserEntity();
            updateUser.setAutoPunchEnabled(enabled);

            int rows = userMapper.update(updateUser, updateWrapper);
            if (rows == 0) {
                log.warn("更新自动打卡开关失败，未找到学号为 {} 的用户", studentId);
            } else {
                log.info("用户 {} 的自动打卡开关已更新为: {}", studentId, enabled);
            }
        } catch (Exception e) {
            log.error("更新自动打卡开关异常 studentId: {}, enabled: {}", studentId, enabled, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }

}
