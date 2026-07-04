package com.campusassistant.remote.spider.emptyclassroom.pojo;

import lombok.Data;

@Data
public class EmptyClassroomFingerprintPayload {

    private String academicYear;

    private String semester;

    private String dayOfWeek;

    private String periodsMask;

    private String weeksMask;

    private String campusId;

    private String building;

    private String roomType;
}