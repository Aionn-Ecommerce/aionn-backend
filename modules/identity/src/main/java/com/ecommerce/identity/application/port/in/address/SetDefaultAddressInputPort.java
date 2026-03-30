package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.SetDefaultAddressCommand;
import com.ecommerce.identity.application.dto.address.AddressResult;

public interface SetDefaultAddressInputPort {
    AddressResult execute(SetDefaultAddressCommand command);
}