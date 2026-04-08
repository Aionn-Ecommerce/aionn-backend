package com.ecommerce.identity.application.port.in.registration;

import com.ecommerce.identity.application.dto.registration.command.ResendRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.result.ResendRegistrationOtpResult;
import com.ecommerce.sharedkernel.application.usecase.CommandUseCase;

public interface ResendRegistrationOtpInputPort
        extends CommandUseCase<ResendRegistrationOtpCommand, ResendRegistrationOtpResult> {
}
