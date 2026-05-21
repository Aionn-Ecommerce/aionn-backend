package com.aionn.identity.infrastructure.registration;

import com.aionn.identity.application.port.out.registration.CaptchaTokenValidator;
import com.aionn.identity.infrastructure.config.properties.RegistrationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development-only captcha verifier. Activates only when explicitly opted-in
 * via {@code identity.registration.captcha.provider=mock} so production cannot
 * accidentally bypass captcha because of an unset env variable.
 */
@Component
@ConditionalOnProperty(prefix = "identity.registration.captcha", name = "provider", havingValue = "mock")
public class MockCaptchaTokenValidator implements CaptchaTokenValidator {

    private final RegistrationProperties properties;

    public MockCaptchaTokenValidator(RegistrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }
        String expected = properties.captcha().expectedToken();
        if (expected == null || expected.isBlank()) {
            // Accept any non-empty token for local dev convenience.
            return true;
        }
        return expected.equals(captchaToken);
    }
}

