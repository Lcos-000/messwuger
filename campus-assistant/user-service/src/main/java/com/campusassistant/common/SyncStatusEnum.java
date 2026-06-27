package com.campusassistant.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SyncStatusEnum {

    NOT_SYNCED(0, "未同步"),

    SYNCING(1, "同步中"),

    SYNCING_SUCCESS(2, "同步成功"),

    SYNCING_FAILED(3, "同步失败");

    private final Integer code;
    private final String desc;

}
