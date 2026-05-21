package com.aionn.identity.infrastructure.persistence.mapper;

import com.aionn.identity.domain.model.DataExportRequest;
import com.aionn.identity.domain.valueobject.DataExportStatus;
import com.aionn.identity.infrastructure.persistence.entity.DataExportRequestEntity;
import com.aionn.identity.infrastructure.persistence.entity.UserEntity;
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



