package com.ecommerce.identity.application.dto.registration.command;

import com.ecommerce.sharedkernel.application.command.Command;

public record VerifyRegistrationOtpCommand(String regId, String otpCode) implements Command {
}


