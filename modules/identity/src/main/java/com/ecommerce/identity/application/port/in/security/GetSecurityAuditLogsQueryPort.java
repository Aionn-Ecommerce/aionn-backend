package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.SecurityAuditLogResult;
import java.util.List;

public interface GetSecurityAuditLogsQueryPort {
    List<SecurityAuditLogResult> execute(String userId);
}