package com.aionn.identity.application.service;

import com.aionn.identity.application.port.out.security.SecurityAuditPort;
import com.aionn.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final SecurityAuditPort securityAuditPort;

        public List<SecurityAuditEntity> getAuditLogs(String userId) {
        log.debug("Retrieving audit logs for user: {}", userId);
        return securityAuditPort.getAuditLogs(userId);
    }
}

