package com.campusassistant.support;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.pojo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GatewayResponseWriter {

    private final ObjectMapper objectMapper;

    public Mono<Void> writeUnauthorized(ServerWebExchange exchange) {
        return writeJson(exchange, HttpStatus.UNAUTHORIZED, Result.error(ResultCodeEnum.UNAUTHORIZED));
    }

    public Mono<Void> writeForbidden(ServerWebExchange exchange) {
        return writeJson(exchange, HttpStatus.FORBIDDEN, Result.error(ResultCodeEnum.FORBIDDEN));
    }

    public Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, Result<?> result) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setCacheControl("no-cache");

        return writeResult(response, result);
    }

    private Mono<Void> writeResult(ServerHttpResponse response, Result<?> result) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(dataBuffer));
        } catch (Exception e) {
            try {
                byte[] fallbackBytes = objectMapper.writeValueAsBytes(Result.error(ResultCodeEnum.SYSTEM_ERROR));
                DataBuffer fallbackBuffer = response.bufferFactory().wrap(fallbackBytes);
                return response.writeWith(Mono.just(fallbackBuffer));
            } catch (Exception ex) {
                return response.setComplete();
            }
        }
    }
}