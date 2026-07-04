package com.campusassistant.utils.converter.personalinfo;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.spider.sync.pojo.entity.PersonalInfoEntity;
import com.campusassistant.remote.spider.sync.pojo.vo.PersonalInfoVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonalInfoVoConvertor extends BaseConvertor<PersonalInfoEntity, PersonalInfoVO> {
}
