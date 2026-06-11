package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record CloneCommand(String sourceId, String ownerId) implements Command {
}
