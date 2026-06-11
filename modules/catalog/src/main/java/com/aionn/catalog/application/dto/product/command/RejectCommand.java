package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record RejectCommand(String productId, String adminId, String reasonCode, String feedback) implements Command {
}
