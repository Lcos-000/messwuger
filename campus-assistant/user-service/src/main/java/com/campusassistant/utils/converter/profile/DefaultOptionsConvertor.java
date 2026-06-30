package com.campusassistant.utils.converter.profile;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.personalization.config.ProfileDefaultOptionsProperties;
import com.campusassistant.personalization.pojo.vo.ProfileDefaultOptionsVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DefaultOptionsConvertor extends BaseConvertor<ProfileDefaultOptionsProperties, ProfileDefaultOptionsVO> {
}
