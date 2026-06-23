package com.campusassistant.remote.exception.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campusassistant.enums.RemoteCodeEnum;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.spider.client.SpiderServiceClient;
import feign.FeignException;
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

        if (cause == null){
            log.error("未知错误，未捕获到异常信息");
        }
        else if (cause instanceof com.alibaba.csp.sentinel.slots.block.BlockException) {
            RemoteCodeEnum blockType = RemoteCodeEnum.getByException((BlockException) cause);
            log.error("远程调用失败：[{}] {}",blockType.getCode(),blockType.getMessage(),cause);
        }
        else if (cause instanceof IOException) {
            log.error("网络故障：无法连接到爬虫服务，message={}", cause.getMessage(), cause);
        }
        // FeignException 是所有 HTTP 错误的父类
        else if (cause instanceof FeignException) {
            int status = ((FeignException) cause).status();
            if (status >= 500) {
                log.error("服务端报错：[{}]爬虫服务内部错误:{}: ", status, cause.getMessage(), cause);
            } else if (status >= 400) {
                switch (status) {
                    case 400:
                        log.warn("参数校验失败: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    case 401:
                        log.warn("认证失效: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    case 403:
                        log.warn("权限不足: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    case 404:
                        log.error("接口不存在: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    default:
                        log.error("其他客户端错误: [{}] {}", status, cause.getMessage(), cause);
                        break;
                }
                }
        }
        else if (cause instanceof TimeoutException) {
            log.error("响应超时：{}",  cause.getMessage(), cause);
        }
        else {
            log.error("未知异常：{}", cause.getMessage(), cause);
        }

        return new SpiderServiceClient() {

            @Override
            public Result<?> verifyAccount(String studentId, String encryptedPassword, String type) {
                return Result.error(ResultCodeEnum.RPC_ERROR.getCode(), "教务爬虫系统暂时不可用，账号验证失败，请稍后再试");
            }

            @Override
            public Result<?> startFullSpiderTask(String studentId, String encryptedPassword, String type) {
                return Result.error(ResultCodeEnum.RPC_ERROR.getCode(), "教务爬虫系统暂时不可用，触发同步失败，请稍后手动刷新");
            }

            @Override
            public Result<?> startPunchCardTask(String studentId, String encryptedPassword, String type) {
                return Result.error(ResultCodeEnum.RPC_ERROR.getCode(), "教务爬虫系统暂时不可用，自动打卡失败，请稍后再试");
            }
        };
    }
}
