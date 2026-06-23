package com.campusassistant.enums;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RemoteCodeEnum {

    // 定义枚举时，绑定具体的异常 Class
    AUTHORITY_DENIED(403, "无权访问该资源", AuthorityException.class),
    FLOW_LIMIT(429, "系统繁忙，请稍后再试", FlowException.class),
    TOO_MANY_REQUESTS(429, "访问过于频繁，请稍后再试", ParamFlowException.class),
    UNKNOWN_BLOCK(500, "系统繁忙，请稍后再试", BlockException.class),
    DEGRADE_LIMIT(503, "服务暂时不可用，正在恢复中", DegradeException.class),
    SYSTEM_BLOCK(503, "系统负载过高，请稍后再试", SystemBlockException.class);

    private final int code;
    private final String message;
    private final Class<? extends BlockException> exceptionClass;


    //根据捕获到的异常，自动匹配对应的枚举
    public static RemoteCodeEnum getByException(BlockException e) {
        if (e == null) {
            return UNKNOWN_BLOCK; // 默认兜底
        }
        for (RemoteCodeEnum type : values()) {
            // 使用 isInstance 进行类型匹配（支持继承关系）
            if (type.getExceptionClass().isInstance(e)) {
                return type;
            }
        }
        // 如果都不匹配，返回一个默认的未知错误枚举
        return UNKNOWN_BLOCK;
    }
}
