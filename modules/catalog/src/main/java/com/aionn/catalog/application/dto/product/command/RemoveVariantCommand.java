package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

public record RemoveVariantCommand(String productId, String ownerId, String skuId) implements Command {
}
