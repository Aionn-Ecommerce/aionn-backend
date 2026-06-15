package com.aionn.catalog.application.dto.review.command;

import com.aionn.sharedkernel.application.command.Command;

public record MerchantReplyCommand(
        String reviewId,
        String ownerId,
        String content) implements Command {
}
