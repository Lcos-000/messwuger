package com.campusassistant.student.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PunchStatusEnum {
    NOT_PUNCHED(0, "未打卡"),
    PUNCHING(1, "打卡中"),
    PUNCH_SUCCESS(2, "打卡成功"),
    PUNCH_FAILED(3, "打卡失败"),

    AUTO_PUNCH_ENABLED(1, "自动打卡开启"),
    AUTO_PUNCH_DISABLED(0, "自动打卡关闭");



    private final Integer code;
    private final String desc;
}