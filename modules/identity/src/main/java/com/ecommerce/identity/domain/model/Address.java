package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.valueobject.AddressType;

import java.time.LocalDateTime;

public record Address(
		String addressId,
		String userId,
		String contactName,
		String phone,
		String provinceCode,
		String provinceName,
		String districtCode,
		String districtName,
		String wardCode,
		String wardName,
		String detailAddress,
		String fullAddress,
		AddressType type,
		boolean isDefault,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public boolean canBeDeleted() {
		return !isDefault;
	}
}
