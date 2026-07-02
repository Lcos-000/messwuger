package com.campusassistant.utils.converter.profile;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.personalization.pojo.entity.UserProfileStyleEntity;
import com.campusassistant.personalization.pojo.UserProfileStyleUpdateDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileStyleDtoConvertor extends BaseConvertor<UserProfileStyleEntity, UserProfileStyleUpdateDTO> {
}
