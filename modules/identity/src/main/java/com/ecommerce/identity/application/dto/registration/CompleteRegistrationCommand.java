package com.ecommerce.identity.application.dto.registration;

import com.ecommerce.sharedkernel.application.command.Command;

public record CompleteRegistrationCommand(String regId, String password, String username, String verificationToken) implements Command {
}
