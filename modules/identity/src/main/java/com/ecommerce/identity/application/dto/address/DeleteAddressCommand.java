package com.ecommerce.identity.application.dto.address;

import com.ecommerce.sharedkernel.application.command.Command;

public record DeleteAddressCommand(String userId, String addressId) implements Command {
}