package com.campusassistant.utils.converter.personalinfo;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.spider.pojo.PersonalInfoEntity;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonalInfoVoConvertor extends BaseConvertor<PersonalInfoEntity, PersonalInfoVO> {
}
