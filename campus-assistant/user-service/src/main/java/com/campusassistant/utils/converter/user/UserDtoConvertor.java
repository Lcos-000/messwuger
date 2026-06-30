package com.campusassistant.utils.converter.user;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.student.pojo.dto.UserDTO;
import com.campusassistant.student.pojo.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoConvertor extends BaseConvertor<UserEntity, UserDTO> {

}
