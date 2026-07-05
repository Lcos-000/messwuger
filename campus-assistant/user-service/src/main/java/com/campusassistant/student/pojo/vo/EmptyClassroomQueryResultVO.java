package com.campusassistant.student.pojo.vo;

import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomConditionDTO;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomItemDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmptyClassroomQueryResultVO extends EmptyClassroomConditionDTO {

    private String queryStatus;

    private Boolean resultReady;

    private List<EmptyClassroomItemDTO> classrooms;
}