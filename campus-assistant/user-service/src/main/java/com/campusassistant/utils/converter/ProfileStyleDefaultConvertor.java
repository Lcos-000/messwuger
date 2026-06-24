package com.campusassistant.utils.converter;

import com.campusassistant.common.converter.BaseConverter;
import com.campusassistant.personalization.config.ProfileDefaultStyleProperties;
import com.campusassistant.personalization.pojo.UserProfileStyleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileStyleDefaultConvertor extends BaseConverter<UserProfileStyleEntity, ProfileDefaultStyleProperties> {
}
