package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record CreateProductCommand(String ownerId, String name) implements Command {
}
