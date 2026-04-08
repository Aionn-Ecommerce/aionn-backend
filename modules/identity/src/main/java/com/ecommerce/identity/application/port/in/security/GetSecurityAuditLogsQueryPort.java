package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.result.SecurityAuditLogResult;
import java.util.List;

public interface GetSecurityAuditLogsQueryPort {
    List<SecurityAuditLogResult> execute(String userId);
}


