package com.campusassistant.constant;


// 统一定义所有微服务共享的硬编码字符串
// 接口里定义的变量默认 public static final
public interface SystemConstants {

    // Header 相关常量
    String HEADER_AUTHORIZATION = "Authorization"; // 客户端传来的 Token 的 Header key
    String HEADER_USER_ID = "X-User-Id";           // 网关向微服务透传的 用户ID Header key
    String HEADER_STUDENT_ID = "X-StudentId";
    String HEADER_USER_ROLE = "X-User-Role";

    // TraceId 相关常量
    String TRACE_ID_HEADER = "Trace-Id"; // Http 请求头中的 key
    String TRACE_ID_MDC = "traceId";     // slf4j MDC 中的 key

    // 网关通讯秘钥常量
    String GATEWAY_TOKEN_HEADER = "Gateway-Token";

    // 身份状态
    String ROLE_USER = "USER";
    String ROLE_ADMIN = "ADMIN";

    // 请求头携带
    String USER_ID = "userid";
    String STUDENT_ID = "studentID";
    String USER_ROLE = "user-role";

}
