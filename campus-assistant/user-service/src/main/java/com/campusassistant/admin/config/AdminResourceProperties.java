package com.campusassistant.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "admin.resources")
public class AdminResourceProperties {

    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {

        private String code;

        private String name;

        private String url;
    }
}