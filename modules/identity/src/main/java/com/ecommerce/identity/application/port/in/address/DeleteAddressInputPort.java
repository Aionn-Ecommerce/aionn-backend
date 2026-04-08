package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.command.DeleteAddressCommand;

public interface DeleteAddressInputPort {
    void execute(DeleteAddressCommand command);
}

