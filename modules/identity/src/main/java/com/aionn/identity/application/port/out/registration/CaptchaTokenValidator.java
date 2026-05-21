package com.aionn.identity.application.port.out.registration;

public interface CaptchaTokenValidator {
    boolean isValid(String captchaToken);
}


