package com.ecommerce.identity.infrastructure.config;

import com.ecommerce.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.ecommerce.identity.application.port.out.registration.CaptchaTokenValidator;
import com.ecommerce.identity.application.port.out.registration.RegistrationOtpSender;
import com.ecommerce.identity.application.port.out.registration.RegistrationRateLimiter;
import com.ecommerce.identity.application.port.out.registration.RegistrationSessionStore;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.application.usecase.registration.CompleteRegistrationUseCase;
import com.ecommerce.identity.application.usecase.registration.InitiateRegistrationUseCase;
import com.ecommerce.identity.application.usecase.registration.VerifyRegistrationOtpUseCase;
import com.ecommerce.identity.domain.repository.IdentityUserRepository;
import com.ecommerce.sharedkernel.application.port.UnitOfWork;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(IdentityRegistrationProperties.class)
@RequiredArgsConstructor
public class IdentityUseCaseConfig {

    private final IdentityUserRepository identityUserRepository;
    private final RegistrationOtpSender registrationOtpSender;
    private final CaptchaTokenValidator captchaTokenValidator;
    private final RegistrationRateLimiter registrationRateLimiter;
    private final RegistrationSessionStore registrationSessionStore;
    private final IdentityRegistrationProperties registrationProperties;
    private final UnitOfWork unitOfWork;

    @Bean
    public InitiateRegistrationInputPort initiateRegistrationUseCase() {
        return new InitiateRegistrationUseCase(
                identityUserRepository,
                registrationOtpSender,
                captchaTokenValidator,
                registrationRateLimiter,
                registrationSessionStore,
                registrationProperties);
    }

    @Bean
    public VerifyRegistrationOtpInputPort verifyRegistrationOtpUseCase() {
        return new VerifyRegistrationOtpUseCase(registrationSessionStore);
    }

    @Bean
    public CompleteRegistrationInputPort completeRegistrationUseCase() {
        return new CompleteRegistrationUseCase(
                registrationSessionStore,
                identityUserRepository,
                unitOfWork,
                passwordHasher(passwordEncoder()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordHasher passwordHasher(PasswordEncoder passwordEncoder) {
        return passwordEncoder::encode;
    }
}