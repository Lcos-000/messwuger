package com.campusassistant.remote.exception.code;

import com.campusassistant.enums.EnumCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseRemoteCodeEnum implements EnumCode {

    COURSE_SERVICE_UNAVAILABLE(541, "课表服务暂时不可用，请稍后重试"),
    COURSE_SERVICE_TIMEOUT(542, "课表服务响应超时，请稍后重试"),
    COURSE_BAD_REQUEST(543, "课表服务请求参数异常"),
    COURSE_AUTH_FAILED(544, "课表服务鉴权失败"),
    COURSE_API_NOT_FOUND(545, "课表服务接口不存在"),
    COURSE_SERVER_ERROR(546, "课表服务内部异常，请稍后重试"),
    COURSE_RPC_ERROR(547, "课表服务调用失败，请稍后重试");

    private final Integer code;
    private final String message;
}
