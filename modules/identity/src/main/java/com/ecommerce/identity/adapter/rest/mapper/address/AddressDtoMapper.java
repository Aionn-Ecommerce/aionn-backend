package com.ecommerce.identity.adapter.rest.mapper.address;

import com.ecommerce.identity.adapter.rest.dto.address.AddressResponse;
import com.ecommerce.identity.adapter.rest.dto.address.CreateAddressRequest;
import com.ecommerce.identity.adapter.rest.dto.address.UpdateAddressRequest;
import com.ecommerce.identity.application.dto.address.*;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.Address;
import com.ecommerce.identity.domain.valueobject.AddressType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressDtoMapper {


	//request -> command
	@Mapping(target = "userId", source = "userId")
	CreateAddressCommand toCreateCommand(String userId, CreateAddressRequest request);

	@Mapping(target = "userId", source = "userId")
	@Mapping(target = "addressId", source = "addressId")
	UpdateAddressCommand toUpdateCommand(String userId, String addressId, UpdateAddressRequest request);

	DeleteAddressCommand toDeleteCommand(String userId, String addressId);

	SetDefaultAddressCommand toSetDefaultCommand(String userId, String addressId);

	//result -> response
	@Mapping(target = "userId", source = "userId")
	@Mapping(target = "fullAddress", source = "fullAddress")
	@Mapping(target = "isDefault", source = "default")
	AddressResult toResult(Address address);

	@Mapping(target = "fullFormattedAddress", source = "fullAddress")
	AddressResponse toResponse(AddressResult result);

	List<AddressResponse> toResponses(List<AddressResult> results);

	//helper convert
	default AddressType mapStringToAddressType(String type) {
		if (type == null) return null;
		try {
			return AddressType.valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IdentityException(IdentityErrorCode.INVALID_ADDRESS_TYPE);
		}
	}
}
