package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.agent.AgentAuditPort;
import com.ecommerce.identity.domain.model.SecurityAudit;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.mapper.SecurityAuditDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.security.SecurityAuditRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter implementation for agent audit log operations.
 * Provides database-level filtering for optimal performance.
 */
@Component
@RequiredArgsConstructor
public class AgentAuditAdapter implements AgentAuditPort {

    private final SecurityAuditRepository securityAuditRepository;
    private final UserRepository userRepository;
    private final SecurityAuditDomainMapper mapper;

    @Override
    public SecurityAudit save(SecurityAudit securityAudit) {
        SecurityAuditEntity entity = mapper.toEntity(securityAudit);

        // Set the user relationship
        UserEntity user = userRepository.getReferenceById(securityAudit.getUserId());
        entity.setUser(user);

        SecurityAuditEntity saved = securityAuditRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<SecurityAudit> findByAgentId(String agentId, int limit) {
        // Use database-level filtering with LIKE query
        // This performs filtering at the database level instead of in-memory
        PageRequest pageRequest = PageRequest.of(0, limit);
        return securityAuditRepository
                .findByDescriptionContainingOrderByTimestampDesc(agentId, pageRequest)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
