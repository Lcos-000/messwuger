package com.campusassistant.remote.spider.emptyclassroom.pojo.dto;

import lombok.Data;

@Data // 查询条件字段封装
public class EmptyClassroomItemDTO {

    private String building;

    private String roomCode;

    private String roomName;

    private String campus;

    private String capacity;

    private String realCapacity;

    private String roomType;

    private String floor;

    private String remark;
}