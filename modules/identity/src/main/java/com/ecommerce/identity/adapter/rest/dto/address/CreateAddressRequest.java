package com.ecommerce.identity.adapter.rest.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAddressRequest(
		@NotBlank(message = "Recipient name is required")
		String contactName,

		@NotBlank(message = "Phone number is required")
		@Pattern(regexp = "^(0|84)([35789])\\d{8}$", message = "Invalid Vietnam phone number format")
		String phone,

		@NotBlank(message = "Province code is required")
		String provinceCode,

		@NotBlank(message = "District code is required")
		String districtCode,

		@NotBlank(message = "Ward code is required")
		String wardCode,

		@NotBlank(message = "Detail address is required")
		String detailAddress,

		@NotBlank(message = "Address type is required")
		@Pattern(regexp = "HOME|OFFICE", message = "Address type must be HOME or OFFICE")
		String type,

		boolean isDefault
) {
}