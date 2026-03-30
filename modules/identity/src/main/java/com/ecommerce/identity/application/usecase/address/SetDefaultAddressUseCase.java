package com.ecommerce.identity.application.usecase.address;

import com.ecommerce.identity.adapter.rest.mapper.address.AddressDtoMapper;
import com.ecommerce.identity.application.dto.address.AddressResult;
import com.ecommerce.identity.application.dto.address.SetDefaultAddressCommand;
import com.ecommerce.identity.application.port.in.address.SetDefaultAddressInputPort;
import com.ecommerce.identity.application.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetDefaultAddressUseCase implements SetDefaultAddressInputPort {

    private final AddressService addressService;
    private final AddressDtoMapper addressMapper;

    @Override
    public AddressResult execute(SetDefaultAddressCommand command) {
        var entity = addressService.setDefaultAddress(command.userId(), command.addressId());
        return addressMapper.toResult(entity);
    }
}
