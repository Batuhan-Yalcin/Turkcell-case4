package com.turkcellcase4.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // LocalDateTime serializer
        javaTimeModule.addSerializer(
            java.time.LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        // LocalDate serializer and deserializer
        javaTimeModule.addSerializer(
            java.time.LocalDate.class,
            new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE)
        );
        javaTimeModule.addDeserializer(
            java.time.LocalDate.class,
            new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)
        );

        return Jackson2ObjectMapperBuilder.json()
            .modules(javaTimeModule)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }
}
