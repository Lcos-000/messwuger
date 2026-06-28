package com.campusassistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCodeEnum implements EnumCode {

    // 常用响应枚举
    SUCCESS(200, "请求成功"),
    NO_CONTENT(204,"操作成功，无返回数据"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "没有权限访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统异常，请稍后再试");


    private final Integer code;
    private final String message;
}
