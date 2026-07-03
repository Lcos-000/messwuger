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

    private static final int MAX_LOG_LENGTH = 1000;
    private static final String ADMIN_LOGS_PREFIX = "/admin/logs";

    private final ObjectMapper objectMapper;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String requestUri = request.getRequestURI();

        if (shouldUseCompactLog(requestUri)) {
            return doCompactLogAround(joinPoint, request, requestUri, startTime);
        }

        return doNormalLogAround(joinPoint, request, requestUri, startTime);
    }

    private Object doCompactLogAround(ProceedingJoinPoint joinPoint,
                                      HttpServletRequest request,
                                      String requestUri,
                                      long startTime) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            log.info("ADMIN_LOG_ACCESS uri={} method={} handler={}.{} ip={} cost={}ms",
                    requestUri,
                    request.getMethod(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    request.getRemoteAddr(),
                    System.currentTimeMillis() - startTime);
            return result;
        } catch (Throwable ex) {
            log.warn("ADMIN_LOG_ACCESS uri={} method={} handler={}.{} ip={} cost={}ms error={}",
                    requestUri,
                    request.getMethod(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    request.getRemoteAddr(),
                    System.currentTimeMillis() - startTime,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private Object doNormalLogAround(ProceedingJoinPoint joinPoint,
                                     HttpServletRequest request,
                                     String requestUri,
                                     long startTime) throws Throwable {
        log.info("================== Request Start ==================");
        log.info("URL            : {}", request.getRequestURL().toString());
        log.info("URI            : {}", requestUri);
        log.info("HTTP Method    : {}", request.getMethod());
        log.info("Class Method   : {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
        log.info("IP             : {}", request.getRemoteAddr());

        logRequestArgs(joinPoint.getArgs());

        Object result = joinPoint.proceed();

        logResponseResult(result);

        log.info("Time Consuming : {} ms", System.currentTimeMillis() - startTime);
        log.info("================== Request End ====================");

        return result;
    }

    private boolean shouldUseCompactLog(String requestUri) {
        return requestUri != null && requestUri.startsWith(ADMIN_LOGS_PREFIX);
    }

    private void logRequestArgs(Object[] args) {
        List<Object> logArgs = new ArrayList<>();

        for (Object arg : args) {
            if (arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile) {
                log.info("Skipping non-serializable arg: {}", arg.getClass().getSimpleName());
                continue;
            }

            if (arg instanceof String strArg) {
                logArgs.add(truncate(strArg));
                continue;
            }

            logArgs.add(arg);
        }

        try {
            String argsJson = objectMapper.writeValueAsString(logArgs);
            log.info("Request Args   : {}", truncate(argsJson));
        } catch (Exception e) {
            log.warn("Request Args   : [参数无法序列化为JSON]");
        }
    }

    private void logResponseResult(Object result) {
        try {
            String resultJson = result == null ? "null" : objectMapper.writeValueAsString(result);
            log.info("Response Result: {}", truncate(resultJson));
        } catch (Exception e) {
            log.warn("Response Result: [结果无法序列化为JSON]");
        }
    }

    private String truncate(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= MAX_LOG_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_LOG_LENGTH) + "... [Truncated]";
    }
}