package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.CreateAddressCommand;
import com.ecommerce.identity.application.dto.address.AddressResult;

public interface CreateAddressInputPort {
    AddressResult execute(CreateAddressCommand command);
}