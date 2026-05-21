package com.aionn.identity.infrastructure.adapter;

import com.aionn.identity.application.port.out.security.SecurityAuditPort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import com.aionn.identity.infrastructure.persistence.repository.security.SecurityAuditRepository;
import com.aionn.identity.infrastructure.persistence.repository.user.UserRepository;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityAuditAdapter implements SecurityAuditPort {

    private final UserRepository userRepository;
    private final SecurityAuditRepository securityAuditRepository;

    @Override
    public void saveAuditLog(String userId, String eventType, String description, String ipAddress) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));

        securityAuditRepository.save(SecurityAuditEntity.builder()
                .auditId(IdGenerator.ulid())
                .user(user)
                .eventType(eventType)
                .description(description)
                .ipAddress(ipAddress)
                .deviceId(null)
                .build());
    }

    @Override
    public List<SecurityAuditEntity> getAuditLogs(String userId) {
        return securityAuditRepository.findTop100ByUser_UserIdOrderByTimestampDesc(userId);
    }
}

