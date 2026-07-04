package com.campusassistant.utils.converter.personalinfo;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.spider.sync.pojo.dto.PersonalInfoDTO;
import com.campusassistant.remote.spider.sync.pojo.entity.PersonalInfoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonalInfoConvertor extends BaseConvertor<PersonalInfoEntity, PersonalInfoDTO> {
}
