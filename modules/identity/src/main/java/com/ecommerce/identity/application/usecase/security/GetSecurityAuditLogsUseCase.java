package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.result.SecurityAuditLogResult;
import com.ecommerce.identity.application.mapper.SecurityResultMapper;
import com.ecommerce.identity.application.port.in.security.GetSecurityAuditLogsQueryPort;
import com.ecommerce.identity.application.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSecurityAuditLogsUseCase implements GetSecurityAuditLogsQueryPort {

    private final SecurityAuditService securityAuditService;
    private final SecurityResultMapper securityResultMapper;

    @Override
    public List<SecurityAuditLogResult> execute(String userId) {
        return securityAuditService.getAuditLogs(userId).stream()
                .map(securityResultMapper::toAuditLogResult)
                .toList();
    }
}
