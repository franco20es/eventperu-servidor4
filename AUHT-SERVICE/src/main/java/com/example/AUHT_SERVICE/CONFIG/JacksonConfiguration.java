package com.example.AUHT_SERVICE.CONFIG;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

// Configuración de Jackson para la serialización/deserialización JSON
@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());                    // ← agrega esto
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ← agrega esto
        return mapper;
    }
}
