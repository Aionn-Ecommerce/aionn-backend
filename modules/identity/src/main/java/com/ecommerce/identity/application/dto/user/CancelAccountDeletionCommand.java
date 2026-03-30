package com.ecommerce.identity.application.dto.user;

import com.ecommerce.sharedkernel.application.command.Command;

public record CancelAccountDeletionCommand(String userId) implements Command {
}
