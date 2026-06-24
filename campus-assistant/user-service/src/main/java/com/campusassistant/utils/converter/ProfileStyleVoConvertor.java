package com.campusassistant.utils.converter;

import com.campusassistant.common.converter.BaseConverter;
import com.campusassistant.personalization.pojo.UserProfileStyleEntity;
import com.campusassistant.personalization.pojo.vo.UserProfileStyleVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileStyleVoConvertor extends BaseConverter<UserProfileStyleEntity, UserProfileStyleVO> {
}
