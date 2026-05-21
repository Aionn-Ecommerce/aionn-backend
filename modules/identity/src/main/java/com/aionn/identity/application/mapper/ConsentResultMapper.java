package com.aionn.identity.application.mapper;

import com.aionn.identity.application.dto.consent.result.ConsentResult;
import com.aionn.identity.infrastructure.persistence.entity.UserConsentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Internal mapper used only by infrastructure adapters to translate consent
 * entities into application-level results. The application layer talks to
 * {@link com.aionn.identity.application.port.out.consent.ConsentPersistencePort}
 * which already returns {@link ConsentResult}, so this mapper does not leak
 * entities into use cases.
 */
@Mapper(componentModel = "spring")
public interface ConsentResultMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "agreed", expression = "java(entity.getRevokedAt() == null)")
    ConsentResult toResult(UserConsentEntity entity);

    List<ConsentResult> toResults(List<UserConsentEntity> entities);
}

