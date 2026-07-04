package com.campusassistant.utils.converter.grade;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.course.pojo.RemoteGradeDTO;
import com.campusassistant.remote.spider.grades.pojo.dto.GradeItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradesDtoConvertor extends BaseConvertor<GradeItemDTO, RemoteGradeDTO> {

    @Override
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "semester", ignore = true)
    RemoteGradeDTO toTarget(GradeItemDTO source);
}