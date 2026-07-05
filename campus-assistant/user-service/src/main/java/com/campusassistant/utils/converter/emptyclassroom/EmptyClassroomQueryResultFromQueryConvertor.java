package com.campusassistant.utils.converter.emptyclassroom;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.student.pojo.dto.EmptyClassroomQueryDTO;
import com.campusassistant.student.pojo.vo.EmptyClassroomQueryResultVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmptyClassroomQueryResultFromQueryConvertor
        extends BaseConvertor<EmptyClassroomQueryDTO, EmptyClassroomQueryResultVO> {

    @Override
    @Mapping(target = "queryStatus", ignore = true)
    @Mapping(target = "resultReady", ignore = true)
    @Mapping(target = "classrooms", ignore = true)
    EmptyClassroomQueryResultVO toTarget(EmptyClassroomQueryDTO source);
}