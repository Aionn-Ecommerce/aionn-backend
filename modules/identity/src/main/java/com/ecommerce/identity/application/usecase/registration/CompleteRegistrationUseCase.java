package com.ecommerce.identity.application.usecase.registration;

import com.ecommerce.identity.application.dto.registration.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationResult;
import com.ecommerce.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.ecommerce.identity.application.port.out.registration.RegistrationSessionStore;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.repository.IdentityUserRepository;
import com.ecommerce.sharedkernel.application.port.UnitOfWork;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompleteRegistrationUseCase implements CompleteRegistrationInputPort {

    private final RegistrationSessionStore registrationSessionStore;
    private final IdentityUserRepository identityUserRepository;
    private final UnitOfWork unitOfWork;
    private final PasswordHasher passwordHasher;

    @Override
    public CompleteRegistrationResult execute(CompleteRegistrationCommand command) {
        var session = registrationSessionStore.findByRegId(command.regId())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.REGISTRATION_NOT_FOUND));

        if (session.isExpired()) {
            throw new IdentityException(IdentityErrorCode.REGISTRATION_EXPIRED);
        }

        if (!session.isVerified() || !command.verificationToken().equals(session.getVerificationToken())) {
            throw new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID);
        }

        var savedUser = unitOfWork.executeInTransaction(() -> {
            IdentityUser user = IdentityUser.createNew(
                    UserId.of(IdGenerator.ulid()),
                    null,
                    session.getPhoneNumber(),
                    command.username());
            user.updatePasswordHash(passwordHasher.hash(command.password()));
            user.updateProfile(command.username(), null);
            user.verifyPhone();
            return identityUserRepository.save(user);
        });

        registrationSessionStore.deleteByRegId(command.regId());

        return new CompleteRegistrationResult(
                savedUser.getUserId(),
                savedUser.getDisplayName(),
                savedUser.getCreatedAt());
    }
}
