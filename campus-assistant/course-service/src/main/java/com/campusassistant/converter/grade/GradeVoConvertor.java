package com.campusassistant.converter.grade;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.pojo.GradeEntity;
import com.campusassistant.pojo.GradeVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GradeVoConvertor extends BaseConvertor<GradeEntity, GradeVO> {
}