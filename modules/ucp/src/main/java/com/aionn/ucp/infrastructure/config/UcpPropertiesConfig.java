package com.aionn.ucp.infrastructure.config;

import com.aionn.ucp.infrastructure.config.properties.UcpCapabilityProperties;
import com.aionn.ucp.infrastructure.config.properties.UcpProfileProperties;
import com.aionn.ucp.infrastructure.config.properties.UcpProtocolProperties;
import com.aionn.ucp.infrastructure.config.properties.UcpSigningProperties;
import com.aionn.ucp.infrastructure.config.properties.UcpValidationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        UcpProtocolProperties.class,
        UcpProfileProperties.class,
        UcpSigningProperties.class,
        UcpCapabilityProperties.class,
        UcpValidationProperties.class
})
public class UcpPropertiesConfig {
}
