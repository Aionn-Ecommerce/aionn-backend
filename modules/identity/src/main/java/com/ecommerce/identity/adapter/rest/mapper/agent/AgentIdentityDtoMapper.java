package com.ecommerce.identity.adapter.rest.mapper.agent;

import com.ecommerce.identity.adapter.rest.dto.agent.AgentAuditLogResponse;
import com.ecommerce.identity.adapter.rest.dto.agent.AgentIdentityResponse;
import com.ecommerce.identity.adapter.rest.dto.agent.CreateAgentIdentityRequest;
import com.ecommerce.identity.adapter.rest.dto.agent.UpdateAgentPermissionsRequest;
import com.ecommerce.identity.application.dto.agent.AgentAuditLogResult;
import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.CreateAgentIdentityCommand;
import com.ecommerce.identity.application.dto.agent.GetAgentAuditLogsQuery;
import com.ecommerce.identity.application.dto.agent.GetAgentIdentityQuery;
import com.ecommerce.identity.application.dto.agent.RevokeAgentCommand;
import com.ecommerce.identity.application.dto.agent.SuspendAgentCommand;
import com.ecommerce.identity.application.dto.agent.UpdateAgentPermissionsCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AgentIdentityDtoMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "agentName", source = "request.agentName")
    CreateAgentIdentityCommand toCreateCommand(String userId, CreateAgentIdentityRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    @Mapping(target = "permissionsJson", source = "request.permissionsJson")
    UpdateAgentPermissionsCommand toUpdatePermissionsCommand(String userId, String agentId,
            UpdateAgentPermissionsRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    SuspendAgentCommand toSuspendCommand(String userId, String agentId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    GetAgentAuditLogsQuery toGetAuditLogsQuery(String userId, String agentId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    GetAgentIdentityQuery toGetAgentQuery(String userId, String agentId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    RevokeAgentCommand toRevokeCommand(String userId, String agentId);

    AgentIdentityResponse toResponse(AgentIdentityResult entity);

    List<AgentIdentityResponse> toResponses(List<AgentIdentityResult> entities);

    List<AgentAuditLogResponse> toAuditLogResponses(List<AgentAuditLogResult> audits);

    AgentAuditLogResponse toAuditLogResponse(AgentAuditLogResult audit);
}
