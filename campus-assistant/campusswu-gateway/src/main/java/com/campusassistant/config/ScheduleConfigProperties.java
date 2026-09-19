package com.campusassistant.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "campus.schedule")
public class ScheduleConfigProperties {

    private String springStartDate;
    private String autumnStartDate;
    private Integer maxWeek;

}
