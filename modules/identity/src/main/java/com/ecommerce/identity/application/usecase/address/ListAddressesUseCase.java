package com.ecommerce.identity.application.usecase.address;

import com.ecommerce.identity.adapter.rest.mapper.address.AddressDtoMapper;
import com.ecommerce.identity.application.dto.address.AddressResult;
import com.ecommerce.identity.application.port.in.address.ListAddressesQueryPort;
import com.ecommerce.identity.application.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAddressesUseCase implements ListAddressesQueryPort {

    private final AddressService addressService;
    private final AddressDtoMapper addressMapper;

    @Override
    public List<AddressResult> execute(String userId) {
        return addressService.listAddressesByUserId(userId).stream()
                .map(addressMapper::toResult)
                .toList();
    }
}
