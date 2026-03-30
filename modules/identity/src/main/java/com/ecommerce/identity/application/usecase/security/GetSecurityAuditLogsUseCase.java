package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.SecurityAuditLogResult;
import com.ecommerce.identity.application.port.in.security.GetSecurityAuditLogsQueryPort;
import com.ecommerce.identity.application.service.SecurityService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetSecurityAuditLogsUseCase implements GetSecurityAuditLogsQueryPort {

    private final SecurityService securityService;

    @Override
    public List<SecurityAuditLogResult> execute(String userId) {
        return securityService.getAuditLogs(userId).stream()
                .map(SecurityResultMapper::toAuditLogResult)
                .toList();
    }
}
