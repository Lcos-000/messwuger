package com.campusassistant.remote.spider.sync.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SyncDataDTO {
    // 基础信息
    private String studentId;
    private String academicYear;
    private String semester;
    @JsonProperty("personalInfo")
    private PersonalInfoDTO personalInfoDTO;
    private Object scheduleData;
}
