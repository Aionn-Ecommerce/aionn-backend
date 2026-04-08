package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.KycProfile;
import com.ecommerce.identity.domain.valueobject.KycStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.KycProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycDomainMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    KycProfileEntity toEntity(KycProfile domain);

    @Mapping(target = "status", expression = "java(KycStatus.valueOf(entity.getStatus()))")
    @Mapping(target = "userId", source = "user.userId")
    KycProfile toDomain(KycProfileEntity entity);
}
