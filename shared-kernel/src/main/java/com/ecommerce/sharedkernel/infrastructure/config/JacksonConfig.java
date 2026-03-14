package com.ecommerce.sharedkernel.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                // Java 8+ date/time support (Instant, LocalDate, etc.)
                .registerModule(new JavaTimeModule())

                // Serialize dates as ISO strings, not epoch millis
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

                // Don't fail on unknown JSON fields (client sends extra fields)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                // Don't fail if a class has no serializable properties
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

                // Exclude null fields from serialized output
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
