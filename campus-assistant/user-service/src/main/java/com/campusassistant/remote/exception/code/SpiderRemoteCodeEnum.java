package com.campusassistant.remote.exception.code;

import com.campusassistant.enums.EnumCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpiderRemoteCodeEnum implements EnumCode {

    SPIDER_SERVICE_UNAVAILABLE(531, "同步服务暂时不可用，请稍后重试"),
    SPIDER_SERVICE_TIMEOUT(532, "同步服务响应超时，请稍后重试"),
    SPIDER_BAD_REQUEST(533, "同步服务请求参数异常"),
    SPIDER_AUTH_FAILED(534, "同步服务鉴权失败"),
    SPIDER_API_NOT_FOUND(535, "同步服务接口不存在"),
    SPIDER_SERVER_ERROR(536, "同步服务内部异常，请稍后重试"),
    SPIDER_RPC_ERROR(537, "同步服务调用失败，请稍后重试");


    private final Integer code;
    private final String message;
}
