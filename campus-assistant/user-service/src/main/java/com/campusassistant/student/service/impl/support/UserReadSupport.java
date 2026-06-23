package com.campusassistant.student.service.impl.support;

import com.campusassistant.student.mapper.UserMapper;
import com.campusassistant.student.pojo.UserEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReadSupport {
    private final UserMapper userMapper;

    // 内部使用，返回 Entity（含 password）
    public UserEntity findEntityByStudentId(String StudentId) {
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getStudentId, StudentId);
        return userMapper.selectOne(lqw);
    }

    public UserEntity findEntityById(Long id) {
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getId, id);
        return userMapper.selectOne(lqw);
    }
}
