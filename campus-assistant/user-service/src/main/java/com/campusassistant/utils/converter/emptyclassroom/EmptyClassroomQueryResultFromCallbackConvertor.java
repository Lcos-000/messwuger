package com.campusassistant.utils.converter.emptyclassroom;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomCallbackDTO;
import com.campusassistant.student.pojo.vo.EmptyClassroomQueryResultVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmptyClassroomQueryResultFromCallbackConvertor
        extends BaseConvertor<EmptyClassroomCallbackDTO, EmptyClassroomQueryResultVO> {

    @Override
    @Mapping(target = "queryStatus", ignore = true)
    @Mapping(target = "resultReady", ignore = true)
    EmptyClassroomQueryResultVO toTarget(EmptyClassroomCallbackDTO source);
}