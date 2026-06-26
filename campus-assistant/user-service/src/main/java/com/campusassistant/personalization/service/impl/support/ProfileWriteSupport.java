package com.campusassistant.personalization.service.impl.support;

import com.campusassistant.common.UserContext;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.personalization.config.ProfileDefaultStyleProperties;
import com.campusassistant.personalization.mapper.UserProfileStyleMapper;
import com.campusassistant.personalization.pojo.entity.UserProfileStyleEntity;
import com.campusassistant.personalization.pojo.UserProfileStyleUpdateDTO;
import com.campusassistant.utils.ThreadLocalUtil;
import com.campusassistant.utils.converter.ProfileStyleDefaultConvertor;
import com.campusassistant.utils.converter.ProfileStyleDtoConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.campusassistant.enums.ResultCodeEnum.SYSTEM_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileWriteSupport {

    private final UserProfileStyleMapper userProfileStyleMapper;
    private final ProfileReadSupport profileReadSupport;
    private final ProfileStyleDtoConvertor profileStyleDtoConvertor;
    private final ProfileStyleDefaultConvertor profileStyleDefaultConvertor;
    private final ProfileDefaultStyleProperties profileDefaultStyleProperties;

    @Transactional(rollbackFor = Exception.class)
    public void initByStudentId() {
        UserContext userContext = ThreadLocalUtil.get();
        String studentId = userContext.getStudentId();
        initByStudentId(studentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void initByStudentId(String studentId) {
        UserProfileStyleEntity exist = profileReadSupport.getByStudentId(studentId);
        if (exist != null) {
            return;
        }

        UserProfileStyleEntity entity = profileStyleDefaultConvertor.toTarget(profileDefaultStyleProperties);
        entity.setStudentId(studentId);
        userProfileStyleMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateByStudentId(UserProfileStyleUpdateDTO updateDTO) {
        UserProfileStyleEntity exist = profileReadSupport.getByStudentId();
        Long id;
        if (exist == null) {
            initByStudentId();
            id = profileReadSupport.getByStudentId().getId();
        }else {
            id = exist.getId();
        }

        try {
            UserProfileStyleEntity updateEntity = profileStyleDtoConvertor.toSource(updateDTO);
            updateEntity.setId(id);
            int rows = userProfileStyleMapper.updateById(updateEntity);
            log.info("已更新用户个性设置，影响行数：{}", rows);
        }catch (Exception e){
            log.error("更新用户个性设置失败", e);
            throw new BusinessException(SYSTEM_ERROR);
        }


    }

}
