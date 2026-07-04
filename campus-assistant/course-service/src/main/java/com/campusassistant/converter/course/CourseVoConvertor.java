package com.campusassistant.converter.course;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.pojo.schedule.CourseEntity;
import com.campusassistant.pojo.schedule.CourseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseVoConvertor extends BaseConvertor<CourseEntity, CourseVO> {
}
