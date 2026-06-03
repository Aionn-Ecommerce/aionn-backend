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

        @Query("SELECT u FROM UserEntity u " +
                        "WHERE (:status IS NULL OR u.status = :status) " +
                        "AND (:role IS NULL OR :role MEMBER OF u.roles) " +
                        "ORDER BY u.createdAt DESC")
        Page<UserEntity> findUsersWithFilters(@Param("status") UserStatus status,
                        @Param("role") UserRole role,
                        Pageable pageable);
}
