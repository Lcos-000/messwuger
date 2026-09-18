package com.campusassistant.remote.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Retryer retryer(){
        // 默认重试机制
        return new Retryer.Default(100,500,3);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        // FULL: 记录请求和响应的头信息、正文和元数据
        // BASIC: 仅记录请求方法、URL、响应状态代码及执行时间（推荐日常调试）
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor spiderServiceTokenInterceptor(
            @Value("${spider.service.token:}") String spiderServiceToken) {
        return requestTemplate -> {
            if (!spiderServiceToken.isBlank()) {
                requestTemplate.header("X-Spider-Token", spiderServiceToken);
            }
        };
    }
}
