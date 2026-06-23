package com.campusassistant.converter;

import com.campusassistant.common.converter.BaseConverter;
import com.campusassistant.pojo.CourseEntity;
import com.campusassistant.pojo.CourseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseVoConverter extends BaseConverter<CourseEntity, CourseVO> {
}
