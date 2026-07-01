package com.campusassistant.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.campusassistant.constant.SystemConstants.*;


@Slf4j
@Configuration
public class FeignHeaderInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        //  获取当前进来的 Spring MVC 原生 HTTP 请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 全线程直接从skywalking获取，不需要透传
            String traceId = TraceContext.traceId();
            // 解析出 UserId 等用户信息，在这里一并透传
            String userId = request.getHeader(HEADER_USER_ID);
            String studentId = request.getHeader(HEADER_STUDENT_ID);
            String userRole = request.getHeader(HEADER_USER_ROLE);
            if (userId != null && !userId.isEmpty()
                && studentId != null && !studentId.isEmpty()
                && userRole != null && !userRole.isEmpty()
            ) {
                template.header(HEADER_USER_ID, userId);
                template.header(HEADER_STUDENT_ID, studentId);
                template.header(HEADER_USER_ROLE, userRole);
            }

            log.debug("Feign 内部调用，追踪 TraceId: {}", traceId);
        }
    }
}