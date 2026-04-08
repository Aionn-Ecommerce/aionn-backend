package com.ecommerce.identity.application.dto.user.command;

import com.ecommerce.sharedkernel.application.command.Command;

public record UpdateDisplayNameCommand(
                String userId,
                String displayName) implements Command {
}
