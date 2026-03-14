package com.ecommerce.identity.infrastructure.persistence.repository;

import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.repository.IdentityUserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdentityUserRepository implements IdentityUserRepository {

    private final Map<String, IdentityUser> usersById = new ConcurrentHashMap<>();

    @Override
    public Optional<IdentityUser> findByEmail(String email) {
        return usersById.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public IdentityUser save(IdentityUser user) {
        usersById.put(user.getUserId(), user);
        return user;
    }
}