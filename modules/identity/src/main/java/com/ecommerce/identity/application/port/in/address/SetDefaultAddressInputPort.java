package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.application.dto.address.command.SetDefaultAddressCommand;

public interface SetDefaultAddressInputPort {
    AddressResult execute(SetDefaultAddressCommand command);
}

