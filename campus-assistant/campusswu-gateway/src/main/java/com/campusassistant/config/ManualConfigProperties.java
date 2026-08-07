package com.campusassistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.manual")
public class ManualConfigProperties {

    private String title;
    private String content;

}
