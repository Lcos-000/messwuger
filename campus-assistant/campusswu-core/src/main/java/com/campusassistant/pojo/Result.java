package com.campusassistant.pojo;

import com.campusassistant.enums.ResultCodeEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // 私有构建方法
    private static <T> Result<T> build(Integer code, String message, T data) {
        return new Result<>(code, message, data, LocalDateTime.now());
    }


    public static <T> Result<T> success() {
        return build(ResultCodeEnum.NO_CONTENT.getCode(), ResultCodeEnum.NO_CONTENT.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return build(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return build(ResultCodeEnum.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> success(ResultCodeEnum resultCodeEnum,T data) {
        return build(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }


    public static <T> Result<T> error(String message) {
        return build(ResultCodeEnum.SYSTEM_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return build(code, message, null);
    }

    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        return build(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), null);
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        return build(code, message, data);
    }
}
