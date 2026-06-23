package com.campusassistant.aspect;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.campusassistant.constant.SystemConstants.HEADER_USER_ROLE;
import static com.campusassistant.constant.SystemConstants.ROLE_ADMIN;

@Aspect
@Component
@Order(1)
public class AdminCheckAspect {

    @Pointcut("@annotation(com.campusassistant.anno.AdminCheck) ||" +
            " @within(com.campusassistant.anno.AdminCheck)")
    public void adminCheckPointcut() {}

    @Before("adminCheckPointcut()")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 防御性编程：如果拿不到请求上下文（比如非 Web 环境调用），直接返回，不报错
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();

        // 获取用户身份
        String role = request.getHeader(HEADER_USER_ROLE);

        // 核心校验逻辑
        if (!ROLE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN.getCode(), "无权访问，需要管理员权限");
        }
    }

}
