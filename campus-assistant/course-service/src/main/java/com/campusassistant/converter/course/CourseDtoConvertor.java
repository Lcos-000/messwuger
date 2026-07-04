package com.campusassistant.converter.course;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.pojo.schedule.CourseDTO;
import com.campusassistant.pojo.schedule.CourseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseDtoConvertor extends BaseConvertor<CourseEntity, CourseDTO> {
    @Mapping(target = "id", ignore = true) // <--- 关键：强制忽略 orderNo
    CourseEntity toSource(CourseDTO courseDTO);
}
