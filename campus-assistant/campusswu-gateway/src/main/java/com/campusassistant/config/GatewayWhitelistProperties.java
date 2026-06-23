package com.campusassistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@RefreshScope // 开启 Nacos 动态刷新
@ConfigurationProperties(prefix = "gatewaylist")
public class GatewayWhitelistProperties {

    // 这里的名字必须和 nacos 里的 whitelist 对应
    private List<String> whitelist;

    private List<String> adminlist;

}
