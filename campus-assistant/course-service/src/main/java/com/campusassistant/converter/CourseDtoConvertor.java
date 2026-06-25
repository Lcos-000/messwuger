package com.campusassistant.converter;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.pojo.CourseDTO;
import com.campusassistant.pojo.CourseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseDtoConvertor extends BaseConvertor<CourseEntity, CourseDTO> {
    @Mapping(target = "id", ignore = true) // <--- 关键：强制忽略 orderNo
    CourseEntity toSource(CourseDTO courseDTO);
}
