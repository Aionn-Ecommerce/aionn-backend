package com.ecommerce.identity.adapter.rest.mapper.agent;

import com.ecommerce.identity.adapter.rest.dto.agent.AgentAuditLogResponse;
import com.ecommerce.identity.adapter.rest.dto.agent.AgentIdentityResponse;
import com.ecommerce.identity.adapter.rest.dto.agent.CreateAgentIdentityRequest;
import com.ecommerce.identity.adapter.rest.dto.agent.UpdateAgentPermissionsRequest;
import com.ecommerce.identity.application.dto.agent.command.CreateAgentIdentityCommand;
import com.ecommerce.identity.application.dto.agent.command.RevokeAgentCommand;
import com.ecommerce.identity.application.dto.agent.command.SuspendAgentCommand;
import com.ecommerce.identity.application.dto.agent.command.UpdateAgentPermissionsCommand;
import com.ecommerce.identity.application.dto.agent.query.GetAgentAuditLogsQuery;
import com.ecommerce.identity.application.dto.agent.query.GetAgentIdentityQuery;
import com.ecommerce.identity.application.dto.agent.result.AgentAuditLogResult;
import com.ecommerce.identity.application.dto.agent.result.AgentIdentityResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AgentIdentityDtoMapperImpl implements AgentIdentityDtoMapper {

    @Override
    public CreateAgentIdentityCommand toCreateCommand(String userId, CreateAgentIdentityRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String agentName = null;
        if ( request != null ) {
            agentName = request.agentName();
        }
        String ownerUserId = null;
        ownerUserId = userId;

        CreateAgentIdentityCommand createAgentIdentityCommand = new CreateAgentIdentityCommand( ownerUserId, agentName );

        return createAgentIdentityCommand;
    }

    @Override
    public UpdateAgentPermissionsCommand toUpdatePermissionsCommand(String userId, String agentId, UpdateAgentPermissionsRequest request) {
        if ( userId == null && agentId == null && request == null ) {
            return null;
        }

        String permissionsJson = null;
        if ( request != null ) {
            permissionsJson = request.permissionsJson();
        }
        String ownerUserId = null;
        ownerUserId = userId;
        String agentId1 = null;
        agentId1 = agentId;

        UpdateAgentPermissionsCommand updateAgentPermissionsCommand = new UpdateAgentPermissionsCommand( ownerUserId, agentId1, permissionsJson );

        return updateAgentPermissionsCommand;
    }

    @Override
    public SuspendAgentCommand toSuspendCommand(String userId, String agentId) {
        if ( userId == null && agentId == null ) {
            return null;
        }

        String ownerUserId = null;
        ownerUserId = userId;
        String agentId1 = null;
        agentId1 = agentId;

        SuspendAgentCommand suspendAgentCommand = new SuspendAgentCommand( ownerUserId, agentId1 );

        return suspendAgentCommand;
    }

    @Override
    public GetAgentAuditLogsQuery toGetAuditLogsQuery(String userId, String agentId) {
        if ( userId == null && agentId == null ) {
            return null;
        }

        String ownerUserId = null;
        ownerUserId = userId;
        String agentId1 = null;
        agentId1 = agentId;

        GetAgentAuditLogsQuery getAgentAuditLogsQuery = new GetAgentAuditLogsQuery( ownerUserId, agentId1 );

        return getAgentAuditLogsQuery;
    }

    @Override
    public GetAgentIdentityQuery toGetAgentQuery(String userId, String agentId) {
        if ( userId == null && agentId == null ) {
            return null;
        }

        String userId1 = null;
        userId1 = userId;
        String agentId1 = null;
        agentId1 = agentId;

        GetAgentIdentityQuery getAgentIdentityQuery = new GetAgentIdentityQuery( userId1, agentId1 );

        return getAgentIdentityQuery;
    }

    @Override
    public RevokeAgentCommand toRevokeCommand(String userId, String agentId) {
        if ( userId == null && agentId == null ) {
            return null;
        }

        String ownerUserId = null;
        ownerUserId = userId;
        String agentId1 = null;
        agentId1 = agentId;

        RevokeAgentCommand revokeAgentCommand = new RevokeAgentCommand( ownerUserId, agentId1 );

        return revokeAgentCommand;
    }

    @Override
    public AgentIdentityResponse toResponse(AgentIdentityResult entity) {
        if ( entity == null ) {
            return null;
        }

        String key = null;
        String agentId = null;
        String permissions = null;
        String status = null;
        LocalDateTime expiryAt = null;
        LocalDateTime createdAt = null;

        key = entity.keyHash();
        agentId = entity.agentId();
        permissions = entity.permissions();
        status = entity.status();
        expiryAt = entity.expiryAt();
        createdAt = entity.createdAt();

        AgentIdentityResponse agentIdentityResponse = new AgentIdentityResponse( agentId, key, permissions, status, expiryAt, createdAt );

        return agentIdentityResponse;
    }

    @Override
    public List<AgentIdentityResponse> toResponses(List<AgentIdentityResult> entities) {
        if ( entities == null ) {
            return null;
        }

        List<AgentIdentityResponse> list = new ArrayList<AgentIdentityResponse>( entities.size() );
        for ( AgentIdentityResult agentIdentityResult : entities ) {
            list.add( toResponse( agentIdentityResult ) );
        }

        return list;
    }

    @Override
    public List<AgentAuditLogResponse> toAuditLogResponses(List<AgentAuditLogResult> audits) {
        if ( audits == null ) {
            return null;
        }

        List<AgentAuditLogResponse> list = new ArrayList<AgentAuditLogResponse>( audits.size() );
        for ( AgentAuditLogResult agentAuditLogResult : audits ) {
            list.add( toAuditLogResponse( agentAuditLogResult ) );
        }

        return list;
    }

    @Override
    public AgentAuditLogResponse toAuditLogResponse(AgentAuditLogResult audit) {
        if ( audit == null ) {
            return null;
        }

        String auditId = null;
        String eventType = null;
        String description = null;
        String ipAddress = null;
        String deviceId = null;
        LocalDateTime timestamp = null;

        auditId = audit.auditId();
        eventType = audit.eventType();
        description = audit.description();
        ipAddress = audit.ipAddress();
        deviceId = audit.deviceId();
        timestamp = audit.timestamp();

        AgentAuditLogResponse agentAuditLogResponse = new AgentAuditLogResponse( auditId, eventType, description, ipAddress, deviceId, timestamp );

        return agentAuditLogResponse;
    }
}
