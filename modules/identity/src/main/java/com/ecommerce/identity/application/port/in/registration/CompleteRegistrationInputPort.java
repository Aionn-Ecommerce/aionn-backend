package com.ecommerce.identity.application.port.in.registration;

import com.ecommerce.identity.application.dto.registration.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationResult;
import com.ecommerce.sharedkernel.application.usecase.CommandUseCase;

public interface CompleteRegistrationInputPort
        extends CommandUseCase<CompleteRegistrationCommand, CompleteRegistrationResult> {
}
