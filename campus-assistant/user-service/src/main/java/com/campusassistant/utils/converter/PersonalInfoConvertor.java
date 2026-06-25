package com.campusassistant.utils.converter;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.spider.pojo.PersonalInfoDTO;
import com.campusassistant.remote.spider.pojo.PersonalInfoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonalInfoConvertor extends BaseConvertor<PersonalInfoEntity, PersonalInfoDTO> {
}
