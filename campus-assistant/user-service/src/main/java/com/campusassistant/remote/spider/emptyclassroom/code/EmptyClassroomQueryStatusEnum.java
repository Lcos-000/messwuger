package com.campusassistant.remote.spider.emptyclassroom.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmptyClassroomQueryStatusEnum {

    QUERYING("QUERYING", "查询中"),
    TIMEOUT("TIMEOUT", "超时"),
    FAILED("FAILED", "失败");

    private final String code;
    private final String description;
}