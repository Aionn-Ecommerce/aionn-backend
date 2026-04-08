package com.ecommerce.identity.application.dto.user.command;

import com.ecommerce.sharedkernel.application.command.Command;

public record ChangePhoneCommand(
                String userId,
                String action,
                String newPhone,
                String otpCode) implements Command {
}
