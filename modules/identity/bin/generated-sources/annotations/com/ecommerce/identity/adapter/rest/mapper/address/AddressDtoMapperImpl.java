package com.ecommerce.identity.adapter.rest.mapper.address;

import com.ecommerce.identity.adapter.rest.dto.address.AddressResponse;
import com.ecommerce.identity.adapter.rest.dto.address.CreateAddressRequest;
import com.ecommerce.identity.adapter.rest.dto.address.UpdateAddressRequest;
import com.ecommerce.identity.application.dto.address.command.CreateAddressCommand;
import com.ecommerce.identity.application.dto.address.command.DeleteAddressCommand;
import com.ecommerce.identity.application.dto.address.command.SetDefaultAddressCommand;
import com.ecommerce.identity.application.dto.address.command.UpdateAddressCommand;
import com.ecommerce.identity.application.dto.address.result.AddressResult;
import com.ecommerce.identity.domain.valueobject.AddressType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:44:21+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AddressDtoMapperImpl implements AddressDtoMapper {

    @Override
    public CreateAddressCommand toCreateCommand(String userId, CreateAddressRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String contactName = null;
        String phone = null;
        String provinceCode = null;
        String districtCode = null;
        String wardCode = null;
        String detailAddress = null;
        boolean isDefault = false;
        if ( request != null ) {
            contactName = request.contactName();
            phone = request.phone();
            provinceCode = request.provinceCode();
            districtCode = request.districtCode();
            wardCode = request.wardCode();
            detailAddress = request.detailAddress();
            isDefault = request.isDefault();
        }
        String userId1 = null;
        userId1 = userId;

        AddressType type = mapStringToAddressType(request.type());

        CreateAddressCommand createAddressCommand = new CreateAddressCommand( userId1, contactName, phone, provinceCode, districtCode, wardCode, detailAddress, type, isDefault );

        return createAddressCommand;
    }

    @Override
    public UpdateAddressCommand toUpdateCommand(String userId, String addressId, UpdateAddressRequest request) {
        if ( userId == null && addressId == null && request == null ) {
            return null;
        }

        String contactName = null;
        String phone = null;
        String provinceCode = null;
        String districtCode = null;
        String wardCode = null;
        String detailAddress = null;
        if ( request != null ) {
            contactName = request.contactName();
            phone = request.phone();
            provinceCode = request.provinceCode();
            districtCode = request.districtCode();
            wardCode = request.wardCode();
            detailAddress = request.detailAddress();
        }
        String userId1 = null;
        userId1 = userId;
        String addressId1 = null;
        addressId1 = addressId;

        AddressType type = mapStringToAddressType(request.type());

        UpdateAddressCommand updateAddressCommand = new UpdateAddressCommand( userId1, addressId1, contactName, phone, provinceCode, districtCode, wardCode, detailAddress, type );

        return updateAddressCommand;
    }

    @Override
    public DeleteAddressCommand toDeleteCommand(String userId, String addressId) {
        if ( userId == null && addressId == null ) {
            return null;
        }

        String userId1 = null;
        userId1 = userId;
        String addressId1 = null;
        addressId1 = addressId;

        DeleteAddressCommand deleteAddressCommand = new DeleteAddressCommand( userId1, addressId1 );

        return deleteAddressCommand;
    }

    @Override
    public SetDefaultAddressCommand toSetDefaultCommand(String userId, String addressId) {
        if ( userId == null && addressId == null ) {
            return null;
        }

        String userId1 = null;
        userId1 = userId;
        String addressId1 = null;
        addressId1 = addressId;

        SetDefaultAddressCommand setDefaultAddressCommand = new SetDefaultAddressCommand( userId1, addressId1 );

        return setDefaultAddressCommand;
    }

    @Override
    public AddressResponse toResponse(AddressResult result) {
        if ( result == null ) {
            return null;
        }

        String addressId = null;
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

        addressId = result.addressId();
        contactName = result.contactName();
        phone = result.phone();
        provinceCode = result.provinceCode();
        provinceName = result.provinceName();
        districtCode = result.districtCode();
        districtName = result.districtName();
        wardCode = result.wardCode();
        wardName = result.wardName();
        detailAddress = result.detailAddress();
        fullAddress = result.fullAddress();
        type = result.type();
        isDefault = result.isDefault();
        createdAt = result.createdAt();
        updatedAt = result.updatedAt();

        AddressResponse addressResponse = new AddressResponse( addressId, contactName, phone, provinceCode, provinceName, districtCode, districtName, wardCode, wardName, detailAddress, fullAddress, type, isDefault, createdAt, updatedAt );

        return addressResponse;
    }

    @Override
    public List<AddressResponse> toResponses(List<AddressResult> results) {
        if ( results == null ) {
            return null;
        }

        List<AddressResponse> list = new ArrayList<AddressResponse>( results.size() );
        for ( AddressResult addressResult : results ) {
            list.add( toResponse( addressResult ) );
        }

        return list;
    }
}
