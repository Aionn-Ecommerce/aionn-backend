package com.aionn.identity.infrastructure.persistence.repository.user;

import com.aionn.identity.domain.valueobject.UserRole;
import com.aionn.identity.domain.valueobject.UserStatus;
import com.aionn.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    boolean existsByPhone(String phone);

    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Finds users with optional filtering by status and role.
     * Uses JOIN FETCH to eagerly load roles and avoid N+1 queries.
     * Supports database-level pagination for efficient querying.
     *
     * @param status   the user status filter (null for no filter)
     * @param role     the user role filter (null for no filter)
     * @param pageable pagination parameters
     * @return page of users matching the criteria
     */
    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.roles r " +
            "WHERE (:status IS NULL OR u.status = :status) " +
            "AND (:role IS NULL OR :role MEMBER OF u.roles) " +
            "ORDER BY u.createdAt DESC")
    Page<UserEntity> findUsersWithFilters(@Param("status") UserStatus status,
            @Param("role") UserRole role,
            Pageable pageable);
}

