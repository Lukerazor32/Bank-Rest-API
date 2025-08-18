package com.example.bankcards.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Конфигурация Jackson для предотвращения циклических ссылок
 * и улучшения сериализации JSON
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Отключаем циклические ссылки
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Регистрируем модуль для работы с Java 8 Time API
        mapper.registerModule(new JavaTimeModule());
        
        // Отключаем запись временных меток
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        return mapper;
    }
} 