package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.security.MfaPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.BackupCodeEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.security.BackupCodeRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MfaPersistenceAdapter implements MfaPersistencePort {

    private final UserRepository userRepository;
    private final BackupCodeRepository backupCodeRepository;

    @Override
    public void updateMfaStatus(String userId, boolean enabled) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        user.setMfaEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public void deleteBackupCodes(String userId) {
        backupCodeRepository.deleteByUser_UserId(userId);
    }

    @Override
    public List<String> saveBackupCodes(String userId, List<String> codeHashes) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));

        List<BackupCodeEntity> entities = codeHashes.stream()
                .map(codeHash -> BackupCodeEntity.builder()
                        .backupCodeId(IdGenerator.ulid())
                        .user(user)
                        .codeHash(codeHash)
                        .build())
                .toList();

        List<BackupCodeEntity> saved = backupCodeRepository.saveAll(entities);
        return saved.stream()
                .map(BackupCodeEntity::getBackupCodeId)
                .toList();
    }
}
