package com.ecommerce.identity.application.dto.address;

import com.ecommerce.sharedkernel.application.command.Command;

public record CreateAddressCommand(
		String userId,
		String contactName,
		String phone,
		String provinceCode,
		String districtCode,
		String wardCode,
		String detailAddress,
		String type,
		boolean isDefault) implements Command {
}
