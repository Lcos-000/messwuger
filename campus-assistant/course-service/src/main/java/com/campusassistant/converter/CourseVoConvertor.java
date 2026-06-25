package com.campusassistant.converter;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.pojo.CourseEntity;
import com.campusassistant.pojo.CourseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseVoConvertor extends BaseConvertor<CourseEntity, CourseVO> {
}
