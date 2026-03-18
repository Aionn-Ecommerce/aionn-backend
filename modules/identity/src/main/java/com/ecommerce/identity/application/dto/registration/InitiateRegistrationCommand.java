package com.ecommerce.identity.application.dto.registration;

import com.ecommerce.sharedkernel.application.command.Command;

public record InitiateRegistrationCommand(String identity, String captchaToken, String ipAddress) implements Command {
}
