package com.campusassistant.remote.exception.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campusassistant.enums.RemoteCodeEnum;
import com.campusassistant.remote.exception.code.SpiderRemoteCodeEnum;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.spider.client.SpiderServiceClient;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class SpiderFallbackFactory implements FallbackFactory<SpiderServiceClient> {

    @Override
    public SpiderServiceClient create(Throwable cause) {
        int code;
        String message;

        if (cause instanceof BlockException blockException) {
            RemoteCodeEnum blockType = RemoteCodeEnum.getByException(blockException);
            code = blockType.getCode();
            message = blockType.getMessage();
            log.error("爬虫服务调用被 Sentinel 拦截：[{}] {}", code, message, cause);
        } else if (cause instanceof RetryableException || cause instanceof TimeoutException) {
            code = SpiderRemoteCodeEnum.SPIDER_SERVICE_TIMEOUT.getCode();
            message = SpiderRemoteCodeEnum.SPIDER_SERVICE_TIMEOUT.getMessage();
            log.error("爬虫服务调用超时：[{}] {}", code, message, cause);
        } else if (cause instanceof IOException) {
            code = SpiderRemoteCodeEnum.SPIDER_SERVICE_UNAVAILABLE.getCode();
            message = SpiderRemoteCodeEnum.SPIDER_SERVICE_UNAVAILABLE.getMessage();
            log.error("爬虫服务不可达：[{}] {}", code, message, cause);
        } else if (cause instanceof FeignException feignException) {
            int status = feignException.status();

            if (status >= 500) {
                code = SpiderRemoteCodeEnum.SPIDER_SERVER_ERROR.getCode();
                message = SpiderRemoteCodeEnum.SPIDER_SERVER_ERROR.getMessage();
                log.error("爬虫服务 5xx 异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
            } else if (status >= 400) {
                switch (status) {
                    case 400 -> {
                        code = SpiderRemoteCodeEnum.SPIDER_BAD_REQUEST.getCode();
                        message = SpiderRemoteCodeEnum.SPIDER_BAD_REQUEST.getMessage();
                        log.warn("爬虫服务 400 参数异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                    case 401, 403 -> {
                        code = SpiderRemoteCodeEnum.SPIDER_AUTH_FAILED.getCode();
                        message = SpiderRemoteCodeEnum.SPIDER_AUTH_FAILED.getMessage();
                        log.error("爬虫服务鉴权失败：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                    case 404 -> {
                        code = SpiderRemoteCodeEnum.SPIDER_API_NOT_FOUND.getCode();
                        message = SpiderRemoteCodeEnum.SPIDER_API_NOT_FOUND.getMessage();
                        log.error("爬虫服务接口不存在：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                    default -> {
                        code = SpiderRemoteCodeEnum.SPIDER_RPC_ERROR.getCode();
                        message = SpiderRemoteCodeEnum.SPIDER_RPC_ERROR.getMessage();
                        log.error("爬虫服务其他 4xx 异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                }
            } else {
                code = SpiderRemoteCodeEnum.SPIDER_RPC_ERROR.getCode();
                message = SpiderRemoteCodeEnum.SPIDER_RPC_ERROR.getMessage();
                log.error("爬虫服务未知 Feign 异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
            }
        } else {
            code = SpiderRemoteCodeEnum.SPIDER_RPC_ERROR.getCode();
            message = SpiderRemoteCodeEnum.SPIDER_RPC_ERROR.getMessage();
            log.error("爬虫服务未知异常：[{}] {}", code, message, cause);
        }

        return new SpiderServiceClient() {

            @Override
            public Result<?> verifyAccount(String studentId, String encryptedPassword, String type) {
                return Result.error(code, message);
            }

            @Override
            public Result<?> startFullSpiderTask(String studentId, String encryptedPassword, String type) {
                return Result.error(code, message);
            }

            @Override
            public Result<?> startPunchCardTask(String studentId, String encryptedPassword, String type) {
                return Result.error(code, message);
            }
        };
    }
}