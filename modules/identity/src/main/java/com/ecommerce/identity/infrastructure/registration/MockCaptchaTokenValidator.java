package com.ecommerce.identity.infrastructure.registration;

import com.ecommerce.identity.application.port.out.registration.CaptchaTokenValidator;
import com.ecommerce.identity.infrastructure.config.IdentityRegistrationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "identity.registration.captcha", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockCaptchaTokenValidator implements CaptchaTokenValidator {

    private final IdentityRegistrationProperties properties;

    public MockCaptchaTokenValidator(IdentityRegistrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(String captchaToken) {
        String expected = properties.getCaptcha().getExpectedToken();
        if (expected == null || expected.isBlank()) {
            return true;
        }
        return expected.equals(captchaToken);
    }
}
