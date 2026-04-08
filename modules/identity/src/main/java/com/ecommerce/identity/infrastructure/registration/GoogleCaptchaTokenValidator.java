package com.ecommerce.identity.infrastructure.registration;

import com.ecommerce.identity.application.port.out.registration.CaptchaTokenValidator;
import com.ecommerce.identity.infrastructure.config.properties.RegistrationProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "identity.registration.captcha", name = "provider", havingValue = "google")
public class GoogleCaptchaTokenValidator implements CaptchaTokenValidator {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RegistrationProperties properties;
    private final RestClient restClient;

    public GoogleCaptchaTokenValidator(RegistrationProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    @Override
    public boolean isValid(String captchaToken) {
        String secret = properties.captcha().googleSecretKey();
        if (secret == null || secret.isBlank() || captchaToken == null || captchaToken.isBlank()) {
            return false;
        }

        Map<String, Object> response = restClient.post()
                .uri(VERIFY_URL)
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body("secret=" + secret + "&response=" + captchaToken)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return false;
        }

        Object success = response.get("success");
        return success instanceof Boolean ok && ok;
    }
}
