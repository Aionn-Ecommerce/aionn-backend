package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.result.AgentAuditLogResult;
import com.ecommerce.identity.domain.model.AgentIdentity;
import com.ecommerce.identity.domain.model.SecurityAudit;
import com.ecommerce.identity.domain.valueobject.AgentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AgentResultMapper {

    @Mapping(source = "id", target = "agentId")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "expiresAt", target = "expiryAt")
    AgentIdentityResult toIdentityResult(AgentIdentity agentIdentity);

    @Mapping(source = "id", target = "auditId")
    AgentAuditLogResult toAuditLogResult(SecurityAudit securityAudit);

    @Named("statusToString")
    default String statusToString(AgentStatus status) {
        return status != null ? status.name() : null;
    }
}