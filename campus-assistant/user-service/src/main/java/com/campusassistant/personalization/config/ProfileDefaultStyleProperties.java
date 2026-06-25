package com.campusassistant.personalization.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "profile.default-style")
public class ProfileDefaultStyleProperties {

    private String avatar;

    private String background;

    private String wallpaper;

    private BigDecimal cardOpacity;

    private BigDecimal cardBlur;

    private Integer globalFontEnabled;
}
