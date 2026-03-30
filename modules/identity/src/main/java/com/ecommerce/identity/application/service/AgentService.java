package com.ecommerce.identity.application.service;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.AgentIdentityEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.agent.AgentIdentityRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.security.SecurityAuditRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentIdentityRepository agentRepository;
    private final UserRepository userRepository;
    private final SecurityAuditRepository auditRepository;

    @Transactional
    public AgentIdentityEntity create(String ownerUserId) {
        var owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        AgentIdentityEntity entity = AgentIdentityEntity.builder()
                .agentId(IdGenerator.ulid())
                .owner(owner)
                .keyHash(IdGenerator.ulid())
                .permissions("{\"scope\":\"basic\"}")
                .status("ACTIVE")
                .expiryAt(LocalDateTime.now().plusYears(1))
                .build();
        return agentRepository.save(entity);
    }

    @Transactional
    public AgentIdentityEntity updatePermissions(String ownerUserId, String agentId, String permissionsJson) {
        AgentIdentityEntity entity = getOwnedAgent(ownerUserId, agentId);
        entity.setPermissions(permissionsJson);
        return agentRepository.save(entity);
    }

    @Transactional
    public AgentIdentityEntity suspend(String ownerUserId, String agentId) {
        AgentIdentityEntity entity = getOwnedAgent(ownerUserId, agentId);
        entity.setStatus("SUSPENDED");
        AgentIdentityEntity saved = agentRepository.save(entity);
        auditRepository.save(SecurityAuditEntity.builder()
                .auditId(IdGenerator.ulid())
                .user(saved.getOwner())
                .eventType("AGENT_SUSPENDED")
                .description("Agent suspended: " + saved.getAgentId())
                .build());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditEntity> getAgentAuditLogs(String ownerUserId, String agentId) {
        getOwnedAgent(ownerUserId, agentId);
        return auditRepository.findTop100ByUser_UserIdOrderByTimestampDesc(ownerUserId).stream()
                .filter(a -> a.getDescription() != null && a.getDescription().contains(agentId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgentIdentityEntity> listMy(String ownerUserId) {
        validateOwnerExists(ownerUserId);
        return agentRepository.findByOwner_UserIdOrderByCreatedAtDesc(ownerUserId);
    }

    @Transactional(readOnly = true)
    public AgentIdentityEntity get(String ownerUserId, String agentId) {
        validateOwnerExists(ownerUserId);
        return getOwnedAgent(ownerUserId, agentId);
    }

    @Transactional
    public void revoke(String ownerUserId, String agentId) {
        AgentIdentityEntity entity = getOwnedAgent(ownerUserId, agentId);
        agentRepository.delete(entity);
    }

    private AgentIdentityEntity getOwnedAgent(String ownerUserId, String agentId) {
        return agentRepository.findByAgentIdAndOwner_UserId(agentId, ownerUserId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND, "Agent not found"));
    }

    private void validateOwnerExists(String ownerUserId) {
        if (!userRepository.existsById(ownerUserId)) {
            throw new IdentityException(IdentityErrorCode.USER_NOT_FOUND);
        }
    }
}
