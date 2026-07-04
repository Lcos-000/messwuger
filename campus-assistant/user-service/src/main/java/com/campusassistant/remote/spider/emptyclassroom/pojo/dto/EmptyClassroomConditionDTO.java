package com.campusassistant.remote.spider.emptyclassroom.pojo.dto;

import lombok.Data;

@Data
public class EmptyClassroomConditionDTO {

    private String academicYear;

    private String semester;

    private String dayOfWeek;

    private String periodsMask;

    private String weeksMask;

    private String campusId;

    private String building;

    private String roomType;
}