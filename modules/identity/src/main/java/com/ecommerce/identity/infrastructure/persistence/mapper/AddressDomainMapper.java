package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.application.dto.address.CreateAddressCommand;
import com.ecommerce.identity.application.dto.address.UpdateAddressCommand;
import com.ecommerce.identity.domain.model.Address;
import com.ecommerce.identity.domain.valueobject.AddressType;
import com.ecommerce.identity.infrastructure.persistence.entity.UserAddressEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressDomainMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "isDefault", source = "isDefault")
    Address toDomain(UserAddressEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "addressId", source = "domain.addressId")
    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "contactName", source = "domain.contactName")
    @Mapping(target = "phone", source = "domain.phone")
    @Mapping(target = "provinceCode", source = "domain.provinceCode")
    @Mapping(target = "provinceName", source = "domain.provinceName")
    @Mapping(target = "districtCode", source = "domain.districtCode")
    @Mapping(target = "districtName", source = "domain.districtName")
    @Mapping(target = "wardCode", source = "domain.wardCode")
    @Mapping(target = "wardName", source = "domain.wardName")
    @Mapping(target = "detailAddress", source = "domain.detailAddress")
    @Mapping(target = "fullAddress", source = "domain.fullAddress")
    @Mapping(target = "type", source = "domain.type")
    @Mapping(target = "isDefault", source = "domain.default")
    @Mapping(target = "createdAt", source = "domain.createdAt")
    @Mapping(target = "updatedAt", source = "domain.updatedAt")
    UserAddressEntity toEntity(Address domain, UserEntity userEntity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "addressId", expression = "java(com.ecommerce.sharedkernel.util.IdGenerator.ulid())")
    @Mapping(target = "user", source = "userEntity")
    @Mapping(target = "isDefault", source = "isDefault")
    @Mapping(target = "contactName", source = "command.contactName")
    @Mapping(target = "phone", source = "command.phone")
    @Mapping(target = "provinceCode", source = "command.provinceCode")
    @Mapping(target = "districtCode", source = "command.districtCode")
    @Mapping(target = "wardCode", source = "command.wardCode")
    @Mapping(target = "detailAddress", source = "command.detailAddress")
    @Mapping(target = "type", source = "command.type")
    @Mapping(target = "fullAddress", ignore = true)
    @Mapping(target = "provinceName", ignore = true)
    @Mapping(target = "districtName", ignore = true)
    @Mapping(target = "wardName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserAddressEntity toEntity(CreateAddressCommand command, UserEntity userEntity, boolean isDefault);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "fullAddress", ignore = true)
    @Mapping(target = "provinceName", ignore = true)
    @Mapping(target = "districtName", ignore = true)
    @Mapping(target = "wardName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "type", source = "type")
    void updateEntityFromCommand(UpdateAddressCommand command, @MappingTarget UserAddressEntity entity);

    default AddressType mapType(String value) {
        return value == null ? null : AddressType.valueOf(value);
    }

    default String mapType(AddressType value) {
        return value == null ? null : value.name();
    }
}
