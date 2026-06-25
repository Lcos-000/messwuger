package com.campusassistant.personalization.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "profile.default-options")
public class ProfileDefaultOptionsProperties {

    private List<String> avatars = new ArrayList<>();

    private List<String> backgrounds = new ArrayList<>();

    private List<String> wallpapers = new ArrayList<>();
}
