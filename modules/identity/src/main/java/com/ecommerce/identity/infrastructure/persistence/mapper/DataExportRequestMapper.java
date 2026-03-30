package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.DataExportRequest;
import com.ecommerce.identity.domain.valueobject.DataExportStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.DataExportRequestEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class DataExportRequestMapper {

    public DataExportRequest toDomain(DataExportRequestEntity entity) {
        return new DataExportRequest(
                entity.getExportRequestId(),
                entity.getUser().getUserId(),
                DataExportStatus.valueOf(entity.getStatus()),
                entity.getRequestedAt(),
                entity.getFileUrl(),
                entity.getCompletedAt());
    }

    public DataExportRequestEntity toEntity(DataExportRequest domain, UserEntity userEntity) {
        return DataExportRequestEntity.builder()
                .exportRequestId(domain.getRequestId())
                .user(userEntity)
                .status(domain.getStatus().name())
                .requestedAt(domain.getRequestedAt())
                .fileUrl(domain.getFileUrl())
                .completedAt(domain.getCompletedAt())
                .build();
    }
}
