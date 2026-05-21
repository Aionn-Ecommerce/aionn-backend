package com.aionn.identity.infrastructure.notification;

import com.aionn.identity.application.port.out.registration.RegistrationOtpSender;
import com.aionn.identity.infrastructure.config.properties.RegistrationProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioRegistrationOtpSender implements RegistrationOtpSender {

    private final RegistrationProperties properties;

    public TwilioRegistrationOtpSender(RegistrationProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (properties.twilio().enabled()) {
            Twilio.init(properties.twilio().accountSid(), properties.twilio().authToken());
            log.info("Twilio SMS sender initialized");
        } else {
            log.info("Twilio SMS sender disabled - OTP will only be available in response if exposeOtpInResponse=true");
        }
    }

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
        if (properties.twilio().enabled()) {
            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(properties.twilio().fromPhoneNumber()),
                    "Your verification code is: " + otpCode)
                    .create();
        }
    }
}

