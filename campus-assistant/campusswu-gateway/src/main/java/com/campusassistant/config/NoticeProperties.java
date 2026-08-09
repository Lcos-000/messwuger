package com.campusassistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "campus.notice")
public class NoticeProperties {

    private Boolean enabled = false;
    private Integer version = 0;
    private String title = "";
    private String content = "";
    private String level = "info";
    private String updatedAt = "";

}

