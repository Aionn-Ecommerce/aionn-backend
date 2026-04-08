package com.ecommerce.identity.application.dto.registration.command;

import com.ecommerce.sharedkernel.application.command.Command;

public record ResendRegistrationOtpCommand(
		String regId,
		String ipAddress) implements Command {
}


