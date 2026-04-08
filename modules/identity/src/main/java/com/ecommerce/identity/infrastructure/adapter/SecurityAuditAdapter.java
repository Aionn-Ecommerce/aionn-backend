package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.security.SecurityAuditPort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.security.SecurityAuditRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
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
