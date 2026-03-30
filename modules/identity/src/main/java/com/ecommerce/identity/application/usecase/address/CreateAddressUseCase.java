package com.ecommerce.identity.application.usecase.address;

import com.ecommerce.identity.adapter.rest.mapper.address.AddressDtoMapper;
import com.ecommerce.identity.application.dto.address.AddressResult;
import com.ecommerce.identity.application.dto.address.CreateAddressCommand;
import com.ecommerce.identity.application.port.in.address.CreateAddressInputPort;
import com.ecommerce.identity.application.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAddressUseCase implements CreateAddressInputPort {

	private final AddressService addressService;
	private final AddressDtoMapper addressMapper;

	@Override
	@Transactional
	public AddressResult execute(CreateAddressCommand command) {
		var entity = addressService.createAddress(command);
		return addressMapper.toResult(entity);
	}
}