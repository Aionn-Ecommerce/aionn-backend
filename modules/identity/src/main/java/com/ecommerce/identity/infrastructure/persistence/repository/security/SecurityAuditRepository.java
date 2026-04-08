package com.ecommerce.identity.infrastructure.persistence.repository.security;

import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAuditRepository extends JpaRepository<SecurityAuditEntity, String> {

    List<SecurityAuditEntity> findTop100ByUser_UserIdOrderByTimestampDesc(String userId);

    /**
     * Finds audit logs by description containing the specified text with
     * database-level filtering.
     * This method performs filtering at the database level for optimal performance.
     *
     * @param descriptionPattern the pattern to search for in the description field
     * @param pageable           pagination information
     * @return list of security audit logs matching the description pattern
     */
    List<SecurityAuditEntity> findByDescriptionContainingOrderByTimestampDesc(String descriptionPattern,
            Pageable pageable);
}
