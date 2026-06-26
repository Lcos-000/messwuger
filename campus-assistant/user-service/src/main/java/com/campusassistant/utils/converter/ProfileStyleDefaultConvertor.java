package com.campusassistant.utils.converter;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.personalization.config.ProfileDefaultStyleProperties;
import com.campusassistant.personalization.pojo.entity.UserProfileStyleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileStyleDefaultConvertor extends BaseConvertor<ProfileDefaultStyleProperties, UserProfileStyleEntity> {
}
