package com.ecommerce.identity.application.usecase.address;

import com.ecommerce.identity.application.dto.address.DeleteAddressCommand;
import com.ecommerce.identity.application.port.in.address.DeleteAddressInputPort;
import com.ecommerce.identity.application.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteAddressUseCase implements DeleteAddressInputPort {

    private final AddressService addressService;

    @Override
    public void execute(DeleteAddressCommand command) {
        addressService.deleteAddress(command.userId(), command.addressId());
    }
}
