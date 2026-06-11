package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record DeactivateCommand(String productId, String ownerId, String reason) implements Command {
}
