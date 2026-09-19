package com.campusassistant.student.code;

import com.campusassistant.enums.EnumCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserEnteringEnum implements EnumCode {
    INVALID_CREDENTIALS(400, "学号或密码错误，请检查后重试"),
    USER_ALREADY_EXISTS(409, "该用户已注册，请勿重复注册"),
    USER_DELETION_IN_PROGRESS(409, "该学号正在清理中，请稍后再注册");



    private final Integer code;
    private final String message;
}
