package com.campusassistant.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.schedule")
public class ScheduleConfigProperties {

    private String springStartDate;
    private String autumnStartDate;
    private Integer maxWeek;

}
