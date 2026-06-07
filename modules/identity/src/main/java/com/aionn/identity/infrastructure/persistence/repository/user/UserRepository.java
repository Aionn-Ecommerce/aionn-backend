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
         * Filtered list of users for admin views.
         *
         * <p>
         * Previously used {@code LEFT JOIN FETCH u.roles} together with
         * {@code Pageable},
         * which triggers Hibernate's HHH000104 warning: the join cartesian-product is
         * loaded
         * entirely into memory and pagination is then applied in Java, breaking the
         * contract
         * of {@link Pageable} and burning memory on large result sets.
         * </p>
         *
         * <p>
         * The fetch join was dropped here. {@code roles} is an EAGER
         * {@code @ElementCollection}
         * with {@code @BatchSize(50)} on {@link UserEntity}, so Hibernate will issue
         * one extra
         * batched {@code SELECT ... IN (?, ?, ...)} for the roles of all users on the
         * current
         * page, which is O(1) extra round-trips per page rather than O(N).
         * </p>
         */
        @Query("SELECT u FROM UserEntity u " +
                        "WHERE (:status IS NULL OR u.status = :status) " +
                        "AND (:role IS NULL OR :role MEMBER OF u.roles) " +
                        "ORDER BY u.createdAt DESC")
        Page<UserEntity> findUsersWithFilters(@Param("status") UserStatus status,
                        @Param("role") UserRole role,
                        Pageable pageable);
}
