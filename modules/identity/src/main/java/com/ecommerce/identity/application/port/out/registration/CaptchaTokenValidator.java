package com.ecommerce.identity.application.port.out.registration;

public interface CaptchaTokenValidator {
    boolean isValid(String captchaToken);
}

