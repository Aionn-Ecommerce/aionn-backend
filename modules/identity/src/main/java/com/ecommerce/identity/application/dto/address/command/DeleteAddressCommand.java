package com.ecommerce.identity.application.dto.address.command;

import com.ecommerce.sharedkernel.application.command.Command;

public record DeleteAddressCommand(String userId, String addressId) implements Command {
}

