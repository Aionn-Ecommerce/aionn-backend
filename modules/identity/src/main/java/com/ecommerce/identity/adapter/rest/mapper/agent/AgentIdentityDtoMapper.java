package com.ecommerce.identity.adapter.rest.mapper.agent;

import com.ecommerce.identity.adapter.rest.dto.agent.AgentAuditLogResponse;
import com.ecommerce.identity.adapter.rest.dto.agent.AgentIdentityResponse;
import com.ecommerce.identity.adapter.rest.dto.agent.CreateAgentIdentityRequest;
import com.ecommerce.identity.adapter.rest.dto.agent.UpdateAgentPermissionsRequest;
import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.command.CreateAgentIdentityCommand;
import com.ecommerce.identity.application.dto.agent.query.GetAgentIdentityQuery;
import com.ecommerce.identity.application.dto.agent.command.RevokeAgentCommand;
import com.ecommerce.identity.application.dto.agent.command.SuspendAgentCommand;
import com.ecommerce.identity.application.dto.agent.command.UpdateAgentPermissionsCommand;
import com.ecommerce.identity.application.dto.agent.query.GetAgentAuditLogsQuery;
import com.ecommerce.identity.application.dto.agent.result.AgentAuditLogResult;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AgentIdentityDtoMapper {

    @Mapping(target = "ownerUserId", source = "userId")
    @Mapping(target = "agentName", source = "request.agentName")
    CreateAgentIdentityCommand toCreateCommand(String userId, CreateAgentIdentityRequest request);

    @Mapping(target = "ownerUserId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    @Mapping(target = "permissionsJson", source = "request.permissionsJson")
    UpdateAgentPermissionsCommand toUpdatePermissionsCommand(String userId, String agentId,
            UpdateAgentPermissionsRequest request);

    @Mapping(target = "ownerUserId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    SuspendAgentCommand toSuspendCommand(String userId, String agentId);

    @Mapping(target = "ownerUserId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    GetAgentAuditLogsQuery toGetAuditLogsQuery(String userId, String agentId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    GetAgentIdentityQuery toGetAgentQuery(String userId, String agentId);

    @Mapping(target = "ownerUserId", source = "userId")
    @Mapping(target = "agentId", source = "agentId")
    RevokeAgentCommand toRevokeCommand(String userId, String agentId);

    @Mapping(target = "key", source = "keyHash")
    AgentIdentityResponse toResponse(AgentIdentityResult entity);

    List<AgentIdentityResponse> toResponses(List<AgentIdentityResult> entities);

    List<AgentAuditLogResponse> toAuditLogResponses(List<AgentAuditLogResult> audits);

    AgentAuditLogResponse toAuditLogResponse(AgentAuditLogResult audit);
}
