package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record SubmitForReviewCommand(String productId, String ownerId) implements Command {
}
