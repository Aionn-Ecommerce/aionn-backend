package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsentResultMapper {
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "agreed", expression = "java(entity.getRevokedAt() == null)")
    ConsentResult toResult(UserConsentEntity entity);

    List<ConsentResult> toResults(List<UserConsentEntity> entities);
}
