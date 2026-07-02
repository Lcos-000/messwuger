package com.campusassistant.filter;

import com.campusassistant.config.GatewayWhitelistProperties;
import com.campusassistant.config.GatewaySecretProperties;
import com.campusassistant.constant.SystemConstants;
import com.campusassistant.support.GatewayResponseWriter;
import com.campusassistant.utils.JwtUtil;
import com.campusassistant.key.TokenCacheKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;
import static com.campusassistant.constant.SystemConstants.*;
import static com.campusassistant.utils.TokenTool.normalizeToken;


@Slf4j
@Component
@Order(0) // 数字越小优先级越高
@RequiredArgsConstructor
public class TokenGlobalFilter implements GlobalFilter {

    //  注入属性配置类
    private final GatewayWhitelistProperties whitelistProperties;
    //  实例化路径匹配器（用于解析 ** 通配符）
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final GatewaySecretProperties gatewaySecretProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenCacheKey tokenCacheKey;
    private final GatewayResponseWriter gatewayResponseWriter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求
        ServerHttpRequest request = exchange.getRequest();
        // 提取请求路径
        String path = request.getPath().toString();

        // 直接向 SkyWalking 的探针索要它当前生成的全局唯一 TraceId！
        String traceId = TraceContext.traceId();
        // 如果系统没挂载探针，TraceContext.traceId() 会返回一个默认的 "[Ignored Trace]" 或空。
        if (!StringUtils.hasText(traceId) || traceId.startsWith("[Ignored")) {
            // 只有当没有探针时，才降级使用自己生成的 UUID
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 动态遍历验证白名单（支持通配符）
        if (whitelistProperties.getWhitelist() != null) {
            boolean isWhitePath = whitelistProperties.getWhitelist().stream()
                    .anyMatch(pattern -> antPathMatcher.match(pattern, path));
            if (isWhitePath) {
                log.info("匹配到白名单放行路径: {}", path);
                // 将 traceId 塞入向下游转发的 Request Header 中
                ServerHttpRequest whiteListRequest = request.mutate()
                        .header(SystemConstants.TRACE_ID_HEADER, traceId)
                        .header(SystemConstants.GATEWAY_TOKEN_HEADER, gatewaySecretProperties.getGatewaySecret())
                        .build();
                return chain.filter((exchange.mutate().request(whiteListRequest).build())); // 匹配成功，直接放行
            }
        }


        // 取出 token
        String token = request.getHeaders().getFirst(HEADER_AUTHORIZATION);
        token = normalizeToken(token);

        if (token == null) {
            log.warn("请求被拦截[traceId:{}]: 缺失请求头, header={}, path={}",
                    traceId, HEADER_AUTHORIZATION, path);
            return gatewayResponseWriter.writeUnauthorized(exchange);
        }
        // 解析 token 拿到 userId,userName,userRole，塞入请求头传给下游
        try {
            Map<String, Object> claims = jwtUtil.parseToken(token);

            String tokenCache = stringRedisTemplate.opsForValue().get(tokenCacheKey.getKey(token));
            if (tokenCache == null) {
                log.warn("请求被拦截[traceId:{}]: token 已失效或已登出, path={}", traceId, path);
                return gatewayResponseWriter.writeUnauthorized(exchange);
            }

            String userId = String.valueOf(claims.get(USER_ID));
            String studentId = String.valueOf(claims.get(STUDENT_ID));
            String userRole = String.valueOf(claims.get(USER_ROLE));
            ServerHttpRequest finalRequest = request.mutate()
                    .header(SystemConstants.TRACE_ID_HEADER, traceId)
                    .header(SystemConstants.GATEWAY_TOKEN_HEADER, gatewaySecretProperties.getGatewaySecret())
                    .header(HEADER_USER_ID, userId)
                    .header(HEADER_STUDENT_ID, studentId)
                    .header(HEADER_USER_ROLE, userRole)
                    .build();
            log.info("Token 验证成功，提取用户身份: id={}, studentId={}, role={}", userId, studentId, userRole);

            ServerWebExchange finalExchange = exchange.mutate().request(finalRequest).build();
            // 验证管理员身份
            boolean needAdminCheck =
                    whitelistProperties.getAdminlist() != null &&
                    whitelistProperties.getAdminlist().stream().anyMatch(pattern -> antPathMatcher.match(pattern, path));
            if (needAdminCheck) {
                if (!ROLE_ADMIN.equals(userRole)) {
                    log.warn("权限拒绝[traceId:{}]: userId={}, studentId={}, role={}, path={}",
                            traceId, userId, studentId, userRole, path);
                    return gatewayResponseWriter.writeForbidden(exchange);
                }
                log.info("管理员权限验证通过: path={}", path);
            }
             else {
            log.info("普通用户路径验证通过: 用户[{}]", studentId);
            }

            // 放行
            return chain.filter(finalExchange);

        } catch (Exception e) {
            log.error("Token 解析失败[traceId:{}]: path={}, message={}", traceId, path, e.getMessage(), e);
            return gatewayResponseWriter.writeUnauthorized(exchange);
        }

    }


}
