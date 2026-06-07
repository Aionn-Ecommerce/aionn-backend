package com.aionn.config;

import com.aionn.sharedkernel.infrastructure.web.security.IpSecurityFilter;
import com.aionn.sharedkernel.infrastructure.web.security.SecurityIpProperties;
import com.aionn.identity.adapter.rest.exception.IdentityAccessDeniedHandler;
import com.aionn.identity.adapter.rest.exception.IdentityAuthenticationEntryPoint;
import com.aionn.identity.infrastructure.security.web.BearerAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@Slf4j
@EnableConfigurationProperties(SecurityIpProperties.class)
@EnableMethodSecurity
public class ApiSecurityConfig {

        public ApiSecurityConfig() {
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        IpSecurityFilter ipSecurityFilter,
                        BearerAuthenticationFilter bearerAuthenticationFilter,
                        IdentityAuthenticationEntryPoint identityAuthenticationEntryPoint,
                        IdentityAccessDeniedHandler identityAccessDeniedHandler)
                        throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource(
                                                http.getSharedObject(
                                                                org.springframework.context.ApplicationContext.class)
                                                                .getBean(SecurityIpProperties.class))))
                                .headers(headers -> headers
                                                .contentTypeOptions(opt -> {
                                                })
                                                .frameOptions(frame -> frame.deny())
                                                .referrerPolicy(ref -> ref.policy(
                                                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/social-login",
                                                                "/api/v1/auth/refresh",
                                                                "/api/v1/security/password-reset-requests",
                                                                "/api/v1/security/password-reset",
                                                                "/api/v1/kyc/webhooks/**",
                                                                "/api/v1/registrations/**",
                                                                "/api/v1/geography/**",
                                                                "/api/v1/payments/webhooks/**",
                                                                "/api/v1/shipping/webhooks/**",
                                                                "/ws/chat/**",
                                                                "/.well-known/ucp",
                                                                "/.well-known/ucp/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**").permitAll()
                                                .requestMatchers(
                                                                "/actuator/health",
                                                                "/actuator/health/**",
                                                                "/actuator/info",
                                                                "/actuator/prometheus")
                                                .permitAll()
                                                .requestMatchers("/actuator/**").denyAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(identityAuthenticationEntryPoint)
                                                .accessDeniedHandler(identityAccessDeniedHandler))
                                .addFilterBefore(ipSecurityFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(bearerAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource(SecurityIpProperties properties) {
                CorsConfiguration cfg = new CorsConfiguration();
                List<String> origins = properties.getCors().getAllowedOrigins().stream()
                                .filter(o -> o != null && !o.isBlank())
                                .toList();
                if (origins.isEmpty()) {
                        log.warn("No CORS allowed origins configured; defaulting to http://localhost:3000");
                        cfg.setAllowedOrigins(List.of("http://localhost:3000"));
                } else {
                        cfg.setAllowedOrigins(origins);
                }
                cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                cfg.setAllowedHeaders(List.of(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Client-Type",
                                "X-Request-Id",
                                "X-Idempotency-Key",
                                "X-Forwarded-For",
                                "Origin"));
                cfg.setExposedHeaders(List.of("X-Request-Id", "Idempotent-Replay"));
                cfg.setAllowCredentials(true);
                cfg.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", cfg);
                return source;
        }
}
