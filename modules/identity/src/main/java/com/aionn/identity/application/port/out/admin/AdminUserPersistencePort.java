package com.aionn.identity.application.port.out.admin;

import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.valueobject.UserRole;
import com.aionn.identity.domain.valueobject.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AdminUserPersistencePort {

    Optional<IdentityUser> findById(String userId);

    IdentityUser save(IdentityUser user);

    Page<IdentityUser> findUsersWithFilters(UserStatus status, UserRole role, Pageable pageable);
}
