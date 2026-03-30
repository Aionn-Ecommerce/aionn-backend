package com.ecommerce.identity.infrastructure.persistence.repository.user;

import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByUsernameIgnoreCase(String username);
}
