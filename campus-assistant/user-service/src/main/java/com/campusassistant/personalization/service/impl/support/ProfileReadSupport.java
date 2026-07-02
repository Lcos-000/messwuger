package com.campusassistant.personalization.service.impl.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusassistant.personalization.config.ProfileDefaultOptionsProperties;
import com.campusassistant.personalization.mapper.UserProfileStyleMapper;
import com.campusassistant.personalization.pojo.entity.UserProfileStyleEntity;
import com.campusassistant.personalization.pojo.vo.ProfileDefaultOptionsVO;
import com.campusassistant.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.campusassistant.utils.converter.profile.DefaultOptionsConvertor;

@Component
@RequiredArgsConstructor
public class ProfileReadSupport {

    private final UserProfileStyleMapper userProfileStyleMapper;
    private final ProfileDefaultOptionsProperties profileDefaultOptionsProperties;
    private final DefaultOptionsConvertor defaultOptionsConvertor;

    public UserProfileStyleEntity getByStudentId() {
        return getByStudentId(UserContextUtil.requireStudentId());
    }

    public UserProfileStyleEntity getByStudentId(String studentId) {
        LambdaQueryWrapper<UserProfileStyleEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserProfileStyleEntity::getStudentId, studentId);

        return userProfileStyleMapper.selectOne(lqw);
    }

    public ProfileDefaultOptionsVO getDefaultOptions() {
        return defaultOptionsConvertor.toTarget(profileDefaultOptionsProperties);
    }

}
