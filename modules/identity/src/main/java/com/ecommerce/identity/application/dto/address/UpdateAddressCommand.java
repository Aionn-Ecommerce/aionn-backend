package com.ecommerce.identity.application.dto.address;

import com.ecommerce.identity.domain.valueobject.AddressType;
import com.ecommerce.sharedkernel.application.command.Command;

public record UpdateAddressCommand(
		String userId,
		String addressId,
		String contactName,
		String phone,
		String provinceCode,
		String districtCode,
		String wardCode,
		String detailAddress,
		AddressType type) implements Command {
}
