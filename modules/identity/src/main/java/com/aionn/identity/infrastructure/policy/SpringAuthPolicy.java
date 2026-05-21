package com.aionn.identity.infrastructure.policy;

import com.aionn.identity.application.port.out.auth.AuthPolicy;
import com.aionn.identity.infrastructure.config.properties.AuthSessionProperties;
import com.aionn.identity.infrastructure.config.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAuthPolicy implements AuthPolicy {

    private final AuthSessionProperties properties;
    private final JwtProperties jwtProperties;

    @Override
    public long getSessionExpiresDays() {
        return properties.expiresDays();
    }

    @Override
    public int getAccessTokenExpiryMinutes() {
        return jwtProperties.accessTokenExpiryMinutes();
    }
}
