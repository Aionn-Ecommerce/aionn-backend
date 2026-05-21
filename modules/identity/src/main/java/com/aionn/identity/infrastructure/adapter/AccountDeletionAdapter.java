package com.aionn.identity.infrastructure.adapter;

import com.aionn.identity.application.dto.user.view.DeletionRequestView;
import com.aionn.identity.application.port.out.user.AccountDeletionPort;
import com.aionn.identity.infrastructure.persistence.entity.AccountDeletionRequestEntity;
import com.aionn.identity.infrastructure.persistence.entity.UserEntity;
import com.aionn.identity.infrastructure.persistence.repository.account.AccountDeletionRequestRepository;
import com.aionn.identity.infrastructure.persistence.repository.user.UserRepository;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountDeletionAdapter implements AccountDeletionPort {

    private final AccountDeletionRequestRepository accountDeletionRequestRepository;
    private final UserRepository userRepository;

    @Override
    public DeletionRequestView save(String userId, LocalDateTime scheduledDeletionAt) {
        UserEntity user = userRepository.getReferenceById(userId);
        AccountDeletionRequestEntity request = AccountDeletionRequestEntity.builder()
                .deletionRequestId(IdGenerator.ulid())
                .user(user)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .scheduledDeletionAt(scheduledDeletionAt)
                .build();
        AccountDeletionRequestEntity saved = accountDeletionRequestRepository.save(request);
        return toView(saved);
    }

    @Override
    public Optional<DeletionRequestView> findPendingByUserId(String userId) {
        return accountDeletionRequestRepository.findByUser_UserIdAndStatus(userId, "PENDING")
                .map(this::toView);
    }

    @Override
    public void cancel(String userId) {
        accountDeletionRequestRepository.findByUser_UserIdAndStatus(userId, "PENDING")
                .ifPresent(request -> {
                    request.setStatus("CANCELED");
                    request.setCanceledAt(LocalDateTime.now());
                    accountDeletionRequestRepository.save(request);
                });
    }

    private DeletionRequestView toView(AccountDeletionRequestEntity entity) {
        return new DeletionRequestView(
                entity.getDeletionRequestId(),
                entity.getStatus(),
                entity.getRequestedAt(),
                entity.getScheduledDeletionAt());
    }
}

