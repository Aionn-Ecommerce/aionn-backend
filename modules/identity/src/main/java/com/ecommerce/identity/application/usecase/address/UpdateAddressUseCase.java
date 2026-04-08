package com.ecommerce.identity.application.usecase.address;

import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.application.dto.address.command.UpdateAddressCommand;
import com.ecommerce.identity.application.mapper.AddressResultMapper;
import com.ecommerce.identity.application.port.in.address.UpdateAddressInputPort;
import com.ecommerce.identity.application.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateAddressUseCase implements UpdateAddressInputPort {

	private final AddressService addressService;
	private final AddressResultMapper addressResultMapper;

	@Override
	@Transactional
	public AddressResult execute(UpdateAddressCommand command) {
		var address = addressService.updateAddress(command);
		return addressResultMapper.toResult(address);
	}
}
