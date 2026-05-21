package com.aionn.identity.infrastructure.adapter;

import com.aionn.identity.application.port.out.security.UserSecurityPort;
import com.aionn.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSecurityAdapter implements UserSecurityPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserSecurityData> findById(String userId) {
        return userRepository.findById(userId)
                .map(user -> new UserSecurityData(user.getUserId(), user.getPasswordHash()));
    }

    @Override
    public Optional<UserSecurityData> findByIdentity(String identity) {
        return userRepository.findByEmailIgnoreCase(identity)
                .or(() -> userRepository.findByPhone(identity))
                .or(() -> userRepository.findByUsernameIgnoreCase(identity))
                .map(user -> new UserSecurityData(user.getUserId(), user.getPasswordHash()));
    }
}

