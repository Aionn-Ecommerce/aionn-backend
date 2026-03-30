package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.DeleteAddressCommand;

public interface DeleteAddressInputPort {
    void execute(DeleteAddressCommand command);
}