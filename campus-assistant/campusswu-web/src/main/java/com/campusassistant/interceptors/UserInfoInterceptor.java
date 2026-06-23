package com.campusassistant.interceptors;

import com.campusassistant.common.UserContext;
import com.campusassistant.constant.SystemConstants;
import com.campusassistant.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

import static com.campusassistant.constant.SystemConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        // 直接向 SkyWalking 的探针索要它当前生成的全局唯一 TraceId！
        String traceId = TraceContext.traceId();
        // 如果系统没挂载探针，TraceContext.traceId() 会返回一个默认的 "[Ignored Trace]" 或空。
        if (traceId == null || traceId.isEmpty() || traceId.startsWith("[Ignored")) {
            // 没有探针时，降级使用自己生成的 UUID
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        // 将 TraceId 放入 slf4j 的 MDC 中
        MDC.put(SystemConstants.TRACE_ID_MDC, traceId);

        String userId = request.getHeader(HEADER_USER_ID);
        String studentId = request.getHeader(HEADER_STUDENT_ID);
        String userRole = request.getHeader(HEADER_USER_ROLE);
        UserContext userContext = new UserContext();
        if (userId!=null){
            userContext.setUserId(Long.valueOf(userId));
            userContext.setStudentId(studentId);
            userContext.setRole(userRole);
            ThreadLocalUtil.set(userContext);
        }
        return true; // 永远放行，因为网关已经拦过了
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        MDC.remove(SystemConstants.TRACE_ID_MDC);
        ThreadLocalUtil.remove(); // 记得清理防内存泄漏
    }
}
