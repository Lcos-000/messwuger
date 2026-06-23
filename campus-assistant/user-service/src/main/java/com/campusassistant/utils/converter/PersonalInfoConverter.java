package com.campusassistant.utils.converter;

import com.campusassistant.common.converter.BaseConverter;
import com.campusassistant.remote.spider.pojo.PersonalInfoDTO;
import com.campusassistant.remote.spider.pojo.PersonalInfoEntity;
import com.campusassistant.remote.spider.pojo.SyncDataDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonalInfoConverter extends BaseConverter<PersonalInfoEntity, PersonalInfoDTO> {
}
