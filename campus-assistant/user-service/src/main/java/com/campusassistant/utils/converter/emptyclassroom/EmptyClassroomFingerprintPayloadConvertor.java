package com.campusassistant.utils.converter.emptyclassroom;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomConditionDTO;
import com.campusassistant.remote.spider.emptyclassroom.pojo.EmptyClassroomFingerprintPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmptyClassroomFingerprintPayloadConvertor extends BaseConvertor<EmptyClassroomConditionDTO, EmptyClassroomFingerprintPayload> {
}