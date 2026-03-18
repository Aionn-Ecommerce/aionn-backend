package com.ecommerce.identity.application.port.in.registration;

import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpResult;
import com.ecommerce.sharedkernel.application.usecase.CommandUseCase;

public interface VerifyRegistrationOtpInputPort
        extends CommandUseCase<VerifyRegistrationOtpCommand, VerifyRegistrationOtpResult> {
}
