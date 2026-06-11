package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record PublishCommand(String productId, String adminId) implements Command {
}
