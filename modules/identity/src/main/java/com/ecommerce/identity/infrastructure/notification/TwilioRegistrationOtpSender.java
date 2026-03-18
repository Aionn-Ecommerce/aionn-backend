package com.ecommerce.identity.infrastructure.notification;

import com.ecommerce.identity.application.port.out.registration.RegistrationOtpSender;
import com.ecommerce.identity.infrastructure.config.IdentityRegistrationProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "identity.registration.twilio", name = "enabled", havingValue = "true")
public class TwilioRegistrationOtpSender implements RegistrationOtpSender {

    private final IdentityRegistrationProperties properties;

    public TwilioRegistrationOtpSender(IdentityRegistrationProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        Twilio.init(properties.getTwilio().getAccountSid(), properties.getTwilio().getAuthToken());
    }

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
        Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(properties.getTwilio().getFromPhoneNumber()),
                        "Your verification code is: " + otpCode)
                .create();
    }
}
