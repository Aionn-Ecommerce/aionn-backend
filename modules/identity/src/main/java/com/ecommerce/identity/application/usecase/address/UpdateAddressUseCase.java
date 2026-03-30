package com.ecommerce.identity.application.usecase.address;

import com.ecommerce.identity.adapter.rest.mapper.address.AddressDtoMapper;
import com.ecommerce.identity.application.dto.address.AddressResult;
import com.ecommerce.identity.application.dto.address.UpdateAddressCommand;
import com.ecommerce.identity.application.port.in.address.UpdateAddressInputPort;
import com.ecommerce.identity.application.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateAddressUseCase implements UpdateAddressInputPort {

	private final AddressService addressService;
	private final AddressDtoMapper addressMapper;

	@Override
	public AddressResult execute(UpdateAddressCommand command) {
		var entity = addressService.updateAddress(command);
		return addressMapper.toResult(entity);
	}
}




