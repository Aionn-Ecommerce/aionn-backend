package com.aionn.identity.application.port.out.security;

import java.util.List;
import com.aionn.identity.infrastructure.persistence.entity.SecurityAuditEntity;

public interface SecurityAuditPort {

    void saveAuditLog(String userId, SecurityAuditEvent event, String ipAddress);

    List<SecurityAuditEntity> getAuditLogs(String userId);
}
