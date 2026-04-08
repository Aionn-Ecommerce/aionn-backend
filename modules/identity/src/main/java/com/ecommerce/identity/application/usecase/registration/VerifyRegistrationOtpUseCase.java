package com.ecommerce.identity.application.usecase.registration;

import com.ecommerce.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.ecommerce.identity.application.dto.registration.command.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import com.ecommerce.identity.application.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyRegistrationOtpUseCase implements VerifyRegistrationOtpInputPort {

    private final RegistrationService registrationService;

    @Override
    @Transactional
    public VerifyRegistrationOtpResult execute(VerifyRegistrationOtpCommand command) {
        return registrationService.verifyOtp(command);
    }
}
