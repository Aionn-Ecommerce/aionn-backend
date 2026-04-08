package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.infrastructure.persistence.entity.UserPreferenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPreferenceDomainMapper {

    @Mapping(target = "user", ignore = true)
    UserPreferenceEntity toEntity(UserPreferenceResult result);

    UserPreferenceResult toResult(UserPreferenceEntity entity);
}
