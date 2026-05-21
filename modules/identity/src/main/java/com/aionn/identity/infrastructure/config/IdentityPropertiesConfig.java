package com.aionn.identity.infrastructure.config;

import com.aionn.identity.infrastructure.config.properties.AddressProperties;
import com.aionn.identity.infrastructure.config.properties.AgentProperties;
import com.aionn.identity.infrastructure.config.properties.AuthProperties;
import com.aionn.identity.infrastructure.config.properties.AuthSessionProperties;
import com.aionn.identity.infrastructure.config.properties.CloudinaryProperties;
import com.aionn.identity.infrastructure.config.properties.JwtProperties;
import com.aionn.identity.infrastructure.config.properties.RegistrationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized configuration-properties registration. Records cannot be both
 * {@code @Component} and {@code @ConfigurationProperties} reliably (Spring
 * autowires the constructor instead of binding), so we drop the
 * {@code @Component}
 * stereotype on the records and register them here.
 */
@Configuration
@EnableConfigurationProperties({
                AddressProperties.class,
                AgentProperties.class,
                AuthProperties.class,
                AuthSessionProperties.class,
                CloudinaryProperties.class,
                JwtProperties.class,
                RegistrationProperties.class
})
public class IdentityPropertiesConfig {

        @org.springframework.context.annotation.Bean
        public org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bcryptPasswordEncoder() {
                return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        }
}
