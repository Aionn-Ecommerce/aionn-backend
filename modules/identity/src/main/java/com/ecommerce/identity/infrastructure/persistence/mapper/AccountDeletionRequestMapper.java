package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.AccountDeletionRequest;
import com.ecommerce.identity.domain.valueobject.AccountDeletionStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.AccountDeletionRequestEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountDeletionRequestMapper {

    public AccountDeletionRequest toDomain(AccountDeletionRequestEntity entity) {
        return new AccountDeletionRequest(
                entity.getDeletionRequestId(),
                entity.getUser().getUserId(),
                AccountDeletionStatus.valueOf(entity.getStatus()),
                entity.getRequestedAt(),
                entity.getScheduledDeletionAt(),
                entity.getCanceledAt());
    }

    public AccountDeletionRequestEntity toEntity(AccountDeletionRequest domain, UserEntity userEntity) {
        return AccountDeletionRequestEntity.builder()
                .deletionRequestId(domain.getRequestId())
                .user(userEntity)
                .status(domain.getStatus().name())
                .requestedAt(domain.getRequestedAt())
                .scheduledDeletionAt(domain.getScheduledDeletionAt())
                .canceledAt(domain.getCanceledAt())
                .build();
    }
}


