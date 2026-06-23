package com.campusassistant.exception;


import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        String msg = ResultCodeEnum.PARAM_ERROR.getMessage();
        // 尝试从异常中提取 BindingResult
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else if (e instanceof BindException) {
            bindingResult = ((BindException) e).getBindingResult();
        }
        // 获取所有错误信息
        if (bindingResult != null && !bindingResult.hasErrors()) {
            // 虽然有异常对象，但里面没有具体的字段错误（极少见，属于防御性判断）
            msg = "请求参数格式不正确，但未检测到具体字段错误";
        } else if (bindingResult != null) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            // 构建一个 Map，key是字段名，value是错误信息
            Map<String, String> errorMap = new LinkedHashMap<>();
            for (FieldError error : fieldErrors) {
                errorMap.put(error.getField(), error.getDefaultMessage());
            }
            // 直接将 errorMap 放入 data 返回
            return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), ResultCodeEnum.PARAM_ERROR.getMessage(), errorMap);
        }
        log.warn("参数校验异常: {}", msg);
        return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(Exception e) {
        log.warn("请求体格式异常: {}", e.getMessage());
        return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), "请求参数格式错误");
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(Exception e) {
        log.warn("缺少必传参数: {}", e.getMessage());
        return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), "缺少必传参数");
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务执行异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统发生未捕获异常", e);
        return Result.error(ResultCodeEnum.SYSTEM_ERROR);
    }
}
