package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.domain.model.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressResultMapper {
    AddressResult toResult(Address address);
}


