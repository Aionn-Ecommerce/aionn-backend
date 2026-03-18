package com.ecommerce.identity.domain.repository;

import com.ecommerce.identity.domain.model.IdentityUser;
import java.util.Optional;

public interface IdentityUserRepository {

    Optional<IdentityUser> findByEmail(String email);

    Optional<IdentityUser> findByPhone(String phone);

    IdentityUser save(IdentityUser user);
}