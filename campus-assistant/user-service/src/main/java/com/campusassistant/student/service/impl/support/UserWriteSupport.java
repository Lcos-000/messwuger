package com.campusassistant.student.service.impl.support;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.utils.converter.UserDtoConverter;
import com.campusassistant.student.mapper.UserMapper;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.pojo.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.campusassistant.enums.ResultCodeEnum.USER_ALREADY_EXISTS;
import static com.campusassistant.remote.spider.common.SyncStatusEnum.NOT_SYNCED;
import static com.campusassistant.student.common.PunchStatusEnum.NOT_PUNCHED;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWriteSupport {

    private final UserMapper userMapper;
    private final UserReadSupport userReadSupport;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDtoConverter UserDtoConverter;

    @Transactional
    public void addUser(UserDTO UserDTO) {
        if (userReadSupport.findEntityByStudentId(UserDTO.getStudentId()) != null) {
            throw new BusinessException(USER_ALREADY_EXISTS);
        }
        UserEntity userEntity = UserDtoConverter.toSource(UserDTO);
        String hash = passwordEncoder.encode(UserDTO.getPassword());
        userEntity.setPassword(hash);
        userEntity.setSyncStatus(NOT_SYNCED.getCode());
        userEntity.setPunchStatus(NOT_PUNCHED.getCode());
        try {
            userMapper.insert(userEntity);
        } catch (DuplicateKeyException e) {
            // 兜底处理：防止并发导致的数据库唯一键冲突
            throw new BusinessException(USER_ALREADY_EXISTS);
        }
    }

    public void updateSyncStatus(String studentId, Integer status) {
        try {
            // 构建更新条件：WHERE student_id = ?
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserEntity::getStudentId, studentId);

            // 构建更新内容：SET sync_status = ?
            UserEntity updateUser = new UserEntity();
            updateUser.setSyncStatus(status);

            // 执行更新
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

    public void updatePunchStatus(String studentId, Integer status) {
        try {
            LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserEntity::getStudentId, studentId);

            UserEntity updateUser = new UserEntity();
            updateUser.setPunchStatus(status);

            userMapper.update(updateUser, updateWrapper);
        } catch (Exception e) {
            log.error("更新打卡状态异常, studentId: {}, status: {}", studentId, status, e);
        }
    }

}
