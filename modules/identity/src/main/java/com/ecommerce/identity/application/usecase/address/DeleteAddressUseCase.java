package com.ecommerce.identity.application.usecase.address;

import com.ecommerce.identity.application.dto.address.command.DeleteAddressCommand;
import com.ecommerce.identity.application.port.in.address.DeleteAddressInputPort;
import com.ecommerce.identity.application.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAddressUseCase implements DeleteAddressInputPort {

    private final AddressService addressService;

    @Override
    @Transactional
    public void execute(DeleteAddressCommand command) {
        addressService.deleteAddress(command.userId(), command.addressId());
    }
}


