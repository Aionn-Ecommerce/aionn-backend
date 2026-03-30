package com.ecommerce.identity.application.dto.user;

import com.ecommerce.sharedkernel.application.command.Command;

public record RequestDataExportCommand(String userId) implements Command {
}
