package com.ecommerce.identity.application.usecase.registration;

import com.ecommerce.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.ecommerce.identity.application.dto.registration.command.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.result.InitiateRegistrationResult;
import com.ecommerce.identity.application.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InitiateRegistrationUseCase implements InitiateRegistrationInputPort {

    private final RegistrationService registrationService;

    @Override
    @Transactional
    public InitiateRegistrationResult execute(InitiateRegistrationCommand command) {
        return registrationService.initiate(command);
    }
}
