package com.campusassistant.remote.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigTest {

    @Test
    void spiderServiceTokenInterceptor_addsConfiguredToken() {
        RequestTemplate template = new RequestTemplate();

        new FeignConfig().spiderServiceTokenInterceptor("shared-secret").apply(template);

        assertThat(template.headers().get("X-Spider-Token")).containsExactly("shared-secret");
    }

    @Test
    void spiderServiceTokenInterceptor_doesNotSendEmptyToken() {
        RequestTemplate template = new RequestTemplate();

        new FeignConfig().spiderServiceTokenInterceptor(" ").apply(template);

        assertThat(template.headers()).doesNotContainKey("X-Spider-Token");
    }
}
