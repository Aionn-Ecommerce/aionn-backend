package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.domain.model.Address;
import com.ecommerce.identity.domain.valueobject.AddressType;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:10+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AddressResultMapperImpl implements AddressResultMapper {

    @Override
    public AddressResult toResult(Address address) {
        if ( address == null ) {
            return null;
        }

        String addressId = null;
        String userId = null;
        String contactName = null;
        String phone = null;
        String provinceCode = null;
        String provinceName = null;
        String districtCode = null;
        String districtName = null;
        String wardCode = null;
        String wardName = null;
        String detailAddress = null;
        String fullAddress = null;
        AddressType type = null;
        boolean isDefault = false;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        addressId = address.addressId();
        userId = address.userId();
        contactName = address.contactName();
        phone = address.phone();
        provinceCode = address.provinceCode();
        provinceName = address.provinceName();
        districtCode = address.districtCode();
        districtName = address.districtName();
        wardCode = address.wardCode();
        wardName = address.wardName();
        detailAddress = address.detailAddress();
        fullAddress = address.fullAddress();
        type = address.type();
        isDefault = address.isDefault();
        createdAt = address.createdAt();
        updatedAt = address.updatedAt();

        AddressResult addressResult = new AddressResult( addressId, userId, contactName, phone, provinceCode, provinceName, districtCode, districtName, wardCode, wardName, detailAddress, fullAddress, type, isDefault, createdAt, updatedAt );

        return addressResult;
    }
}
