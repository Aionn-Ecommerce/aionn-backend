package com.ecommerce.identity.application.port.in.registration;

import com.ecommerce.identity.application.dto.registration.result.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.command.CompleteRegistrationCommand;
import com.ecommerce.sharedkernel.application.usecase.CommandUseCase;

public interface CompleteRegistrationInputPort
                extends CommandUseCase<CompleteRegistrationCommand, CompleteRegistrationResult> {
}


