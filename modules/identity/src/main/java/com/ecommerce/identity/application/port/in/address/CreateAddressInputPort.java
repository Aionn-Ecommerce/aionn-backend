package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.application.dto.address.command.CreateAddressCommand;

public interface CreateAddressInputPort {
    AddressResult execute(CreateAddressCommand command);
}

