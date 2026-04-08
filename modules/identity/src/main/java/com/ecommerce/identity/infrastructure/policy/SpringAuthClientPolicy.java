package com.ecommerce.identity.infrastructure.policy;

import com.ecommerce.identity.application.port.out.auth.AuthClientPolicy;
import com.ecommerce.identity.infrastructure.config.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAuthClientPolicy implements AuthClientPolicy {

    private final AuthProperties authProperties;

    @Override
    public String getClientTypeHeader() {
        return authProperties.clientTypeHeader();
    }

    @Override
    public String getMobileClientValue() {
        return authProperties.mobileClientValue();
    }
}
