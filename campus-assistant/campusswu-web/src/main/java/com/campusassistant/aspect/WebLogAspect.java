package com.campusassistant.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class WebLogAspect {

    private final ObjectMapper objectMapper;
    private static final int MAX_LOG_LENGTH = 1000;


    // 拦截所有类上带有 @RestController 注解的方法
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void webLog() {
    }

    // 环绕通知：在方法执行前和执行后都会走这里
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取当前 HTTP 请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null; // 断言不可能为空
        HttpServletRequest request = attributes.getRequest();

        // 打印请求前置日志
        log.info("================== Request Start ==================");
        log.info("URL            : {}", request.getRequestURL().toString());
        log.info("HTTP Method    : {}", request.getMethod());
        log.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        log.info("IP             : {}", request.getRemoteAddr());

        // 过滤无法序列化的参数对象，防止抛出 JsonProcessingException
        Object[] args = joinPoint.getArgs();
        List<Object> logArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse || arg instanceof MultipartFile) {
                // 如果是特殊对象，只打印类型名称，不序列化它的内容
                log.info("Skipping non-serializable arg: {}", arg.getClass().getSimpleName());
                continue; // 直接跳过本次循环，不加入 logArgs
            }
            // 判断字符串长度，太长截断
            if (arg instanceof String strArg) {
                if (strArg.length() > MAX_LOG_LENGTH) {
                    logArgs.add(strArg.substring(0, MAX_LOG_LENGTH) + "... [Truncated]");
                } else {
                    logArgs.add(strArg);
                }
            }
            else {
                logArgs.add(arg);
            }
        }
        // 打印过滤后的安全参数
        try {
            String argsJson = objectMapper.writeValueAsString(logArgs);
            // 【核心修改】如果太长，就截断
            if (argsJson.length() > MAX_LOG_LENGTH) {
                argsJson = argsJson.substring(0, MAX_LOG_LENGTH) + "... [Truncated]";
            }
            log.info("Request Args   : {}", argsJson);
        } catch (Exception e) {
            log.warn("Request Args   : [参数无法序列化为JSON]");
        }

        Object result = joinPoint.proceed();

        // 打印响应结果（如果结果为 null，防止序列化报错）
        try {
            log.info("Response Result: {}", result == null ? "null" : objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            log.warn("Response Result: [结果无法序列化为JSON]");
        }

        log.info("Time Consuming : {} ms", System.currentTimeMillis() - startTime);
        log.info("================== Request End ====================");


        //  将结果返回给前端
        return result;
    }
}
