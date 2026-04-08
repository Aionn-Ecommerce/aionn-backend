package com.ecommerce.identity.infrastructure.user;

import com.ecommerce.identity.application.port.out.user.UserOtpChallengeStore;
import com.ecommerce.identity.application.port.out.user.UserOtpPurpose;
import com.ecommerce.identity.application.port.out.user.model.UserOtpChallenge;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUserOtpChallengeStore implements UserOtpChallengeStore {

    private final Map<String, UserOtpChallenge> storage = new ConcurrentHashMap<>();

    @Override
    public void save(UserOtpChallenge challenge) {
        storage.put(key(challenge.userId(), challenge.purpose()), challenge);
    }

    @Override
    public Optional<UserOtpChallenge> find(String userId, UserOtpPurpose purpose) {
        return Optional.ofNullable(storage.get(key(userId, purpose)));
    }

    @Override
    public void delete(String userId, UserOtpPurpose purpose) {
        storage.remove(key(userId, purpose));
    }

    private String key(String userId, UserOtpPurpose purpose) {
        return userId + ":" + purpose.name();
    }
}


