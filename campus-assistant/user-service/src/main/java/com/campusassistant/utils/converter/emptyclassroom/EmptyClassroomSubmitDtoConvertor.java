package com.campusassistant.utils.converter.emptyclassroom;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomTaskSubmitDTO;
import com.campusassistant.student.pojo.dto.EmptyClassroomQueryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmptyClassroomSubmitDtoConvertor extends BaseConvertor<EmptyClassroomQueryDTO, EmptyClassroomTaskSubmitDTO> {
}