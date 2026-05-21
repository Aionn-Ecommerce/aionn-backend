package com.aionn.identity.application.mapper;

import com.aionn.identity.application.dto.kyc.result.KycResult;
import com.aionn.identity.domain.model.KycProfile;
import com.aionn.identity.infrastructure.persistence.entity.KycProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycResultMapper {

    @Mapping(target = "userId", source = "user.userId")
    KycResult toResult(KycProfileEntity entity);

    KycResult toResult(KycProfile domain);
}

