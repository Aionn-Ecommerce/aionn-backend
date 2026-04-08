package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.dto.user.view.DataExportRequestView;
import com.ecommerce.identity.application.port.out.user.DataExportPort;
import com.ecommerce.identity.domain.valueobject.DataExportStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.DataExportRequestEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.account.DataExportRequestRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataExportAdapter implements DataExportPort {

    private final DataExportRequestRepository dataExportRequestRepository;
    private final UserRepository userRepository;

    @Override
    public DataExportRequestView save(String userId) {
        UserEntity user = userRepository.getReferenceById(userId);
        DataExportRequestEntity request = DataExportRequestEntity.builder()
                .exportRequestId(IdGenerator.ulid())
                .user(user)
                .status(DataExportStatus.REQUESTED.name())
                .requestedAt(LocalDateTime.now())
                .build();
        DataExportRequestEntity saved = dataExportRequestRepository.save(request);
        return toView(saved);
    }

    @Override
    public boolean hasActiveRequest(String userId) {
        return dataExportRequestRepository.existsByUser_UserIdAndStatusIn(
                userId,
                List.of(DataExportStatus.REQUESTED.name(), DataExportStatus.PROCESSING.name()));
    }

    private DataExportRequestView toView(DataExportRequestEntity entity) {
        return new DataExportRequestView(
                entity.getExportRequestId(),
                entity.getStatus(),
                entity.getRequestedAt());
    }
}
