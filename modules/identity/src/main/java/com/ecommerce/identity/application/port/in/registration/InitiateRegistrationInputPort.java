package com.ecommerce.identity.application.port.in.registration;

import com.ecommerce.identity.application.dto.registration.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationResult;
import com.ecommerce.sharedkernel.application.usecase.CommandUseCase;

public interface InitiateRegistrationInputPort
        extends CommandUseCase<InitiateRegistrationCommand, InitiateRegistrationResult> {
}
