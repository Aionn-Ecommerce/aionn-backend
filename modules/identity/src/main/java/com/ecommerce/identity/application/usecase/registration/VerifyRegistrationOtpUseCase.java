package com.ecommerce.identity.application.usecase.registration;

import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpResult;
import com.ecommerce.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.ecommerce.identity.application.port.out.registration.RegistrationSessionStore;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerifyRegistrationOtpUseCase implements VerifyRegistrationOtpInputPort {

    private final RegistrationSessionStore registrationSessionStore;

    @Override
    public VerifyRegistrationOtpResult execute(VerifyRegistrationOtpCommand command) {
        var session = registrationSessionStore.findByRegId(command.regId())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.REGISTRATION_NOT_FOUND));

        try {
            session.verify(command.otpCode());
        } finally {
            registrationSessionStore.save(session);
        }

        return new VerifyRegistrationOtpResult(session.getRegId(), session.getVerificationToken());
    }
}
