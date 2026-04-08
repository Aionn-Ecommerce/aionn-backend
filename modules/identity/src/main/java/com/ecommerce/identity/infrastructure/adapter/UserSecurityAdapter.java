package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.security.UserSecurityPort;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
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
