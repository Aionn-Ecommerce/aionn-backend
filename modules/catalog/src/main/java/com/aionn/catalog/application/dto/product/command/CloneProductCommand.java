package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record CloneProductCommand(String sourceId, String merchantId) implements Command {
}
