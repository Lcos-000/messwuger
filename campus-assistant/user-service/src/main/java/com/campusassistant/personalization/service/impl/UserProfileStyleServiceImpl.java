package com.campusassistant.personalization.service.impl;

import com.campusassistant.personalization.pojo.entity.UserProfileStyleEntity;
import com.campusassistant.personalization.pojo.UserProfileStyleUpdateDTO;
import com.campusassistant.personalization.pojo.vo.ProfileDefaultOptionsVO;
import com.campusassistant.personalization.pojo.vo.UserProfileStyleVO;
import com.campusassistant.personalization.service.UserProfileStyleService;
import com.campusassistant.personalization.service.impl.support.ProfileReadSupport;
import com.campusassistant.personalization.service.impl.support.ProfileWriteSupport;
import com.campusassistant.utils.converter.profile.ProfileStyleVoConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileStyleServiceImpl implements UserProfileStyleService {

    private final ProfileStyleVoConvertor profileStyleVoConvertor;
    private final ProfileReadSupport profileReadSupport;
    private final ProfileWriteSupport profileWriteSupport;

    @Override
    public UserProfileStyleVO getByStudentId() {
        UserProfileStyleEntity entity = profileReadSupport.getByStudentId();
        if (entity == null) {
            return null;
        }
        return profileStyleVoConvertor.toTarget(entity);
    }

    @Override
    public void initByStudentId() {
        profileWriteSupport.initByStudentId();
    }

    @Override
    public void updateByStudentId(UserProfileStyleUpdateDTO updateDTO) {
        profileWriteSupport.updateByStudentId(updateDTO);
    }

    @Override
    public ProfileDefaultOptionsVO getDefaultOptions() {
        return profileReadSupport.getDefaultOptions();
    }

}