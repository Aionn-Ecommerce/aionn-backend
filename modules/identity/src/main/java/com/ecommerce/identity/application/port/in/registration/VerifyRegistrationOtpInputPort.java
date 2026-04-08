package com.ecommerce.identity.application.port.in.registration;

import com.ecommerce.identity.application.dto.registration.command.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import com.ecommerce.sharedkernel.application.usecase.CommandUseCase;

public interface VerifyRegistrationOtpInputPort
        extends CommandUseCase<VerifyRegistrationOtpCommand, VerifyRegistrationOtpResult> {
}
