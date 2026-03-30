package com.ecommerce.identity.application.usecase.registration;

import com.ecommerce.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationResult;
import com.ecommerce.identity.application.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteRegistrationUseCase implements CompleteRegistrationInputPort {

    private final RegistrationService registrationService;

    @Override
    @Transactional
    public CompleteRegistrationResult execute(CompleteRegistrationCommand command) {
        return registrationService.complete(command);
    }
}
