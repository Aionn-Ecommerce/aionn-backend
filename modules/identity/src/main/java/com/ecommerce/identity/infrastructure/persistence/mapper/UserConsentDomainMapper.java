package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.UserConsent;
import com.ecommerce.identity.domain.valueobject.ConsentType;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between UserConsent domain model and
 * UserConsentEntity.
 */
@Mapper(componentModel = "spring", imports = { ConsentType.class })
public interface UserConsentDomainMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "consentType", expression = "java(domain.getConsentType().name())")
    @Mapping(target = "consentId", source = "id")
    UserConsentEntity toEntity(UserConsent domain);

    @Mapping(target = "consentType", expression = "java(ConsentType.valueOf(entity.getConsentType()))")
    @Mapping(target = "id", source = "consentId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "granted", expression = "java(entity.getRevokedAt() == null)")
    UserConsent toDomain(UserConsentEntity entity);
}
