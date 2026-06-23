package com.campusassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({
        "com.campusassistant.student.mapper",
        "com.campusassistant.remote.spider.mapper"
})
@EnableFeignClients(basePackages = {
        "com.campusassistant.remote.course.client",
        "com.campusassistant.remote.spider.client"
}) // 开启远程调用，扫描 @FeignClient 接口
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling //开启定时任务
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}

