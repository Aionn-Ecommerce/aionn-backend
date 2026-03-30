package com.ecommerce.identity.application.port.in.address;

import com.ecommerce.identity.application.dto.address.UpdateAddressCommand;
import com.ecommerce.identity.application.dto.address.AddressResult;

public interface UpdateAddressInputPort {
    AddressResult execute(UpdateAddressCommand command);
}