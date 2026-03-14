package com.ecommerce.identity.application.usecase;

import com.ecommerce.identity.application.dto.RegisterIdentityUserCommand;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.repository.IdentityUserRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterIdentityUserUseCase {

    private final IdentityUserRepository identityUserRepository;

    public RegisterIdentityUserUseCase(IdentityUserRepository identityUserRepository) {
        this.identityUserRepository = identityUserRepository;
    }

    public IdentityUser register(RegisterIdentityUserCommand command) {
        if (command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        identityUserRepository.findByEmail(command.email()).ifPresent(existing -> {
            throw new IllegalStateException("Email already exists");
        });

        IdentityUser user = new IdentityUser(
                UUID.randomUUID().toString(),
                command.email().trim().toLowerCase(),
                command.displayName() == null || command.displayName().isBlank() ? "new-user"
                        : command.displayName().trim(),
                LocalDateTime.now());

        return identityUserRepository.save(user);
    }
}