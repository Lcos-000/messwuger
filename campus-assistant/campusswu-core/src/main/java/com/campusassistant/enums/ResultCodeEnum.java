package com.campusassistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCodeEnum {

    // 常用响应枚举
    SUCCESS(200, "请求成功"),
    NO_CONTENT(204,"操作成功，无返回数据"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "没有权限访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统异常，请稍后再试"),
    // 微服务专栏
    AUTHORITY_DENIED(403, "无权访问该资源"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    FLOW_LIMIT(429, "当前访问人数过多，被限流"),
    RPC_ERROR(501, "远程服务调用失败"),
    SYSTEM_BLOCK(503, "系统负载过高，请稍后再试"),
    DEGRADE_LIMIT(503, "服务不可用，请稍后再试"),
    //用户服务
    INVALID_CREDENTIALS(400, "学号或密码错误，请检查后重试"),
    USER_ALREADY_EXISTS(409, "该用户已注册，请勿重复注册");



    private final Integer code;
    private final String message;
}
