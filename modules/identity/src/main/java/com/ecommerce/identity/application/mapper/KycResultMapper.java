package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.domain.model.KycProfile;
import com.ecommerce.identity.infrastructure.persistence.entity.KycProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycResultMapper {

    @Mapping(target = "userId", source = "user.userId")
    KycResult toResult(KycProfileEntity entity);

    KycResult toResult(KycProfile domain);
}
