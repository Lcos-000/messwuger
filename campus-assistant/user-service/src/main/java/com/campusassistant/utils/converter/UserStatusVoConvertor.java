package com.campusassistant.utils.converter;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.pojo.UserStatusVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStatusVoConvertor extends BaseConvertor<UserEntity, UserStatusVO> {

}
