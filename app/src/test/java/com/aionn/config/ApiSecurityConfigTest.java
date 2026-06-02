package com.aionn.config;

import com.aionn.sharedkernel.infrastructure.web.security.SecurityIpProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiSecurityConfigTest {

    private final ApiSecurityConfig apiSecurityConfig = new ApiSecurityConfig();

    @Test
    void corsConfigurationFallsBackWhenOriginsContainOnlyBlanks() {
        SecurityIpProperties properties = new SecurityIpProperties();
        properties.getCors().setAllowedOrigins(List.of("", "   "));

        CorsConfigurationSource source = apiSecurityConfig.corsConfigurationSource(properties);
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(List.of("http://localhost:3000"), configuration.getAllowedOrigins());
    }

    @Test
    void corsConfigurationExposesTracingAndIdempotencyHeaders() {
        SecurityIpProperties properties = new SecurityIpProperties();
        properties.getCors().setAllowedOrigins(List.of("https://frontend.example"));

        CorsConfigurationSource source = apiSecurityConfig.corsConfigurationSource(properties);
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(List.of("X-Request-Id", "Idempotent-Replay"), configuration.getExposedHeaders());
    }
}
