package com.campusassistant.remote.spider.client;

import com.campusassistant.config.FeignConfig;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.exception.fallback.SpiderFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.campusassistant.remote.common.Constant.*;

@FeignClient(
        name = "campus-spider-service", // 注册中心里 Go 服务的名字
        url = "http://127.0.0.1:8082", // 本地联调 URL
        configuration = FeignConfig.class, // 通用 Feign 配置
        fallbackFactory = SpiderFallbackFactory.class
)
public interface SpiderServiceClient {

    // 1. 注册时调用：仅校验教务账号密码是否正确
    // Go端逻辑：模拟登录教务处，登录成功直接返回200，失败返回错误信息。不爬课表。
    @PostMapping("/api/v1/task/submit")
    Result<?> verifyAccount(
            @RequestHeader(X_Student_Id) String studentId,
            @RequestHeader(X_Password) String password,
            @RequestHeader(TYPE) String type
    );

    // 2. 登录/刷新时调用：全量爬取数据并回推
    // Go端逻辑：模拟登录，爬取个人信息、课表、成绩，最后回推给 Java。
    @PostMapping("/api/v1/task/submit")
    Result<?> startFullSpiderTask(
            @RequestHeader(X_Student_Id) String studentId,
            @RequestHeader(X_Password) String encryptedPassword,
            @RequestHeader(TYPE) String type
    );

    // 3. 自动打卡调用
    @PostMapping("/api/v1/task/punch-card")
    Result<?> startPunchCardTask(
            @RequestHeader(X_Student_Id) String studentId,
            @RequestHeader(X_Password) String encryptedPassword,
            @RequestHeader(TYPE) String type
    );
}