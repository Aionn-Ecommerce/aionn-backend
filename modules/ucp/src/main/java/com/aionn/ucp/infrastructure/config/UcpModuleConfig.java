package com.aionn.ucp.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(UcpProperties.class)
@PropertySource("classpath:application-ucp.yml")
public class UcpModuleConfig {
}
