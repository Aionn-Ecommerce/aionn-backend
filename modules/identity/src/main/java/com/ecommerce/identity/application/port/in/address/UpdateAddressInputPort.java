package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.application.dto.address.command.UpdateAddressCommand;

public interface UpdateAddressInputPort {
    AddressResult execute(UpdateAddressCommand command);
}

