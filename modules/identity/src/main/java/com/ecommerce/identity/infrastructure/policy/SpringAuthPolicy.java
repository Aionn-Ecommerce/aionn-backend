package com.ecommerce.identity.infrastructure.policy;

import com.ecommerce.identity.application.port.out.auth.AuthPolicy;
import com.ecommerce.identity.infrastructure.config.properties.AuthSessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAuthPolicy implements AuthPolicy {

    private final AuthSessionProperties properties;

    @Override
    public long getSessionExpiresDays() {
        return properties.expiresDays();
    }
}
