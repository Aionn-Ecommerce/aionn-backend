package com.ecommerce.identity.infrastructure.persistence.repository.account;

import com.ecommerce.identity.infrastructure.persistence.entity.AccountDeletionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountDeletionRequestRepository extends JpaRepository<AccountDeletionRequestEntity, String> {

    Optional<AccountDeletionRequestEntity> findByUser_UserIdAndStatus(String userId, String status);
}


