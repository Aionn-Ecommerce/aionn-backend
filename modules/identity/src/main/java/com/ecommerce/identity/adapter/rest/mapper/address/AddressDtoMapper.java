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

	// request -> command
	@Mapping(target = "type", expression = "java(mapStringToAddressType(request.type()))")
	CreateAddressCommand toCreateCommand(String userId, CreateAddressRequest request);

	@Mapping(target = "type", expression = "java(mapStringToAddressType(request.type()))")
	UpdateAddressCommand toUpdateCommand(String userId, String addressId, UpdateAddressRequest request);

	DeleteAddressCommand toDeleteCommand(String userId, String addressId);

	SetDefaultAddressCommand toSetDefaultCommand(String userId, String addressId);

	// result -> response
	AddressResult toResult(Address address);

	AddressResponse toResponse(AddressResult result);

	List<AddressResponse> toResponses(List<AddressResult> results);

	// helper convert - validates and converts String to AddressType enum
	default AddressType mapStringToAddressType(String type) {
		if (type == null)
			return null;
		try {
			return AddressType.valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IdentityException(IdentityErrorCode.INVALID_ADDRESS_TYPE);
		}
	}
}
