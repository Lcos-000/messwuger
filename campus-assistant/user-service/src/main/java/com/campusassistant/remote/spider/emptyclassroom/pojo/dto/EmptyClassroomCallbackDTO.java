package com.campusassistant.remote.spider.emptyclassroom.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmptyClassroomCallbackDTO extends EmptyClassroomConditionDTO {

    private String studentId;

    private List<EmptyClassroomItemDTO> classrooms;
}