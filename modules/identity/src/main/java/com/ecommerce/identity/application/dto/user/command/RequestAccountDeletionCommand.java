package com.ecommerce.identity.application.dto.user.command;

import com.ecommerce.sharedkernel.application.command.Command;

public record RequestAccountDeletionCommand(String userId) implements Command {
}


