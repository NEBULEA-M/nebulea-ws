package com.nebulea.ws.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Value("${ros-bridge.url}")
    private String rosBridgeUrl;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean String rosBridgeUrl() {
        return rosBridgeUrl;
    }
}
