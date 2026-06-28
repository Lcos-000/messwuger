package com.campusassistant.exception;

import com.campusassistant.enums.EnumCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code; //父类 RuntimeException 里面没有状态码变量 code 所以要自己定义

    public BusinessException(EnumCode enumCode) {
        super(enumCode.getMessage());
        this.code = enumCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
