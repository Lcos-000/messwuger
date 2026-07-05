package com.campusassistant.converter.grade;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.pojo.grade.GradeEntity;
import com.campusassistant.pojo.grade.dto.GradeItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeDtoConvertor extends BaseConvertor<GradeEntity, GradeItemDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "syncTime", ignore = true)
    GradeEntity toSource(GradeItemDTO source);
}