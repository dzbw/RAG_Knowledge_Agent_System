package com.java1234.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 提供全局 {@link ObjectMapper}，供业务类（如 {@link com.java1234.service.impl.ChatServiceImpl}）注入。
 */
@Configuration
public class JacksonConfig {

    /**
     * 与 Spring 生态兼容的 JSON 处理器（含 Java 8 时间等模块自动发现）。
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}
