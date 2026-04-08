package com.ecommerce.identity.infrastructure.registration;

import com.ecommerce.identity.application.port.out.registration.CaptchaTokenValidator;
import com.ecommerce.identity.infrastructure.config.properties.RegistrationProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "identity.registration.captcha", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockCaptchaTokenValidator implements CaptchaTokenValidator {

    private final RegistrationProperties properties;

    public MockCaptchaTokenValidator(RegistrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(String captchaToken) {
        String expected = properties.captcha().expectedToken();
        if (expected == null || expected.isBlank()) {
            return true;
        }
        return expected.equals(captchaToken);
    }
}
