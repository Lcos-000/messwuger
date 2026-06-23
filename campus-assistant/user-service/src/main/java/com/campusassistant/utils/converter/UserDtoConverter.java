package com.campusassistant.utils.converter;

import com.campusassistant.common.converter.BaseConverter;
import com.campusassistant.student.pojo.dto.UserDTO;
import com.campusassistant.student.pojo.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoConverter extends BaseConverter<UserEntity, UserDTO> {

}
