package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.application.dto.address.CreateAddressCommand;
import com.ecommerce.identity.application.dto.address.UpdateAddressCommand;
import com.ecommerce.identity.domain.model.Address;
import com.ecommerce.identity.domain.valueobject.AddressType;
import com.ecommerce.identity.infrastructure.persistence.entity.UserAddressEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-31T10:39:59+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AddressDomainMapperImpl implements AddressDomainMapper {

    @Override
    public Address toDomain(UserAddressEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String userId = null;
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

        userId = entityUserUserId( entity );
        addressId = entity.getAddressId();
        contactName = entity.getContactName();
        phone = entity.getPhone();
        provinceCode = entity.getProvinceCode();
        provinceName = entity.getProvinceName();
        districtCode = entity.getDistrictCode();
        districtName = entity.getDistrictName();
        wardCode = entity.getWardCode();
        wardName = entity.getWardName();
        detailAddress = entity.getDetailAddress();
        fullAddress = entity.getFullAddress();
        type = mapType( entity.getType() );
        if ( entity.getIsDefault() != null ) {
            isDefault = entity.getIsDefault();
        }
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        Address address = new Address( addressId, userId, contactName, phone, provinceCode, provinceName, districtCode, districtName, wardCode, wardName, detailAddress, fullAddress, type, isDefault, createdAt, updatedAt );

        return address;
    }

    @Override
    public UserAddressEntity toEntity(CreateAddressCommand command, UserEntity userEntity, boolean isDefault, String fullAddress, String provinceName, String districtName, String wardName) {
        if ( command == null && userEntity == null && fullAddress == null && provinceName == null && districtName == null && wardName == null ) {
            return null;
        }

        UserAddressEntity.UserAddressEntityBuilder userAddressEntity = UserAddressEntity.builder();

        if ( command != null ) {
            userAddressEntity.phone( command.phone() );
            userAddressEntity.contactName( command.contactName() );
            userAddressEntity.provinceCode( command.provinceCode() );
            userAddressEntity.districtCode( command.districtCode() );
            userAddressEntity.wardCode( command.wardCode() );
            userAddressEntity.detailAddress( command.detailAddress() );
            userAddressEntity.type( mapType( command.type() ) );
        }
        userAddressEntity.user( userEntity );
        userAddressEntity.isDefault( isDefault );
        userAddressEntity.fullAddress( fullAddress );
        userAddressEntity.provinceName( provinceName );
        userAddressEntity.districtName( districtName );
        userAddressEntity.wardName( wardName );
        userAddressEntity.addressId( com.ecommerce.sharedkernel.util.IdGenerator.ulid() );

        return userAddressEntity.build();
    }

    @Override
    public void updateEntityFromCommand(UpdateAddressCommand command, UserAddressEntity entity, String fullAddress, String provinceName, String districtName, String wardName) {
        if ( command == null && fullAddress == null && provinceName == null && districtName == null && wardName == null ) {
            return;
        }

        if ( command != null ) {
            entity.setContactName( command.contactName() );
            entity.setPhone( command.phone() );
            entity.setProvinceCode( command.provinceCode() );
            entity.setDistrictCode( command.districtCode() );
            entity.setWardCode( command.wardCode() );
            entity.setDetailAddress( command.detailAddress() );
            entity.setType( mapType( command.type() ) );
        }
        entity.setFullAddress( fullAddress );
        entity.setProvinceName( provinceName );
        entity.setDistrictName( districtName );
        entity.setWardName( wardName );
    }

    private String entityUserUserId(UserAddressEntity userAddressEntity) {
        UserEntity user = userAddressEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
