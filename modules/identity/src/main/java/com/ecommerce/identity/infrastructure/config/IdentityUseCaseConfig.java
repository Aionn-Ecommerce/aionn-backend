package com.ecommerce.identity.infrastructure.config;

import com.ecommerce.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.ecommerce.identity.application.port.in.admin.RemoveUserRolesInputPort;
import com.ecommerce.identity.application.port.in.admin.GetUserByIdQueryPort;
import com.ecommerce.identity.application.port.in.admin.ListUsersQueryPort;
import com.ecommerce.identity.application.port.in.admin.UpdateUserRolesInputPort;
import com.ecommerce.identity.application.port.in.admin.UpdateUserStatusInputPort;
import com.ecommerce.identity.application.port.in.agent.CreateAgentIdentityInputPort;
import com.ecommerce.identity.application.port.in.agent.GetAgentAuditLogsQueryPort;
import com.ecommerce.identity.application.port.in.agent.GetAgentIdentityQueryPort;
import com.ecommerce.identity.application.port.in.agent.ListMyAgentIdentitiesQueryPort;
import com.ecommerce.identity.application.port.in.agent.RevokeAgentInputPort;
import com.ecommerce.identity.application.port.in.agent.SuspendAgentInputPort;
import com.ecommerce.identity.application.port.in.agent.UpdateAgentPermissionsInputPort;
import com.ecommerce.identity.application.port.in.consent.AgreePrivacyInputPort;
import com.ecommerce.identity.application.port.in.consent.AgreeTermsInputPort;
import com.ecommerce.identity.application.port.in.consent.GetMyConsentsQueryPort;
import com.ecommerce.identity.application.port.in.consent.UpdateMarketingConsentInputPort;
import com.ecommerce.identity.application.port.in.kyc.ApproveKycInputPort;
import com.ecommerce.identity.application.port.in.kyc.CancelKycInputPort;
import com.ecommerce.identity.application.port.in.kyc.CreateKycInputPort;
import com.ecommerce.identity.application.port.in.kyc.GetKycQueryPort;
import com.ecommerce.identity.application.port.in.kyc.ListMyKycQueryPort;
import com.ecommerce.identity.application.port.in.kyc.RejectKycInputPort;
import com.ecommerce.identity.application.port.in.kyc.ReviewKycInputPort;
import com.ecommerce.identity.application.port.in.kyc.SubmitKycInputPort;
import com.ecommerce.identity.application.port.in.kyc.UploadKycDocumentInputPort;
import com.ecommerce.identity.application.port.in.preference.UpdateAiPrivacyPreferenceInputPort;
import com.ecommerce.identity.application.port.in.preference.GetUserPreferenceQueryPort;
import com.ecommerce.identity.application.port.in.preference.UpdateGeneralPreferenceInputPort;
import com.ecommerce.identity.application.port.in.preference.UpdateNotificationPreferenceInputPort;
import com.ecommerce.identity.application.port.in.security.ChangePasswordInputPort;
import com.ecommerce.identity.application.port.in.security.CompletePasswordResetInputPort;
import com.ecommerce.identity.application.port.in.security.DisableMfaInputPort;
import com.ecommerce.identity.application.port.in.security.EnableMfaInputPort;
import com.ecommerce.identity.application.port.in.security.GetSecurityAuditLogsQueryPort;
import com.ecommerce.identity.application.port.in.security.RegenerateBackupCodesInputPort;
import com.ecommerce.identity.application.port.in.security.RequestPasswordResetInputPort;
import com.ecommerce.identity.application.port.in.security.UnlockAccountInputPort;
import com.ecommerce.identity.application.port.out.registration.RegistrationPolicy;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.application.service.ConsentService;
import com.ecommerce.identity.application.service.KycService;
import com.ecommerce.identity.application.service.PreferenceService;
import com.ecommerce.identity.application.service.AddressService;
import com.ecommerce.identity.application.service.AdminUserService;
import com.ecommerce.identity.application.service.AgentService;
import com.ecommerce.identity.application.service.RegistrationService;
import com.ecommerce.identity.application.service.SecurityService;
import com.ecommerce.identity.application.usecase.admin.RemoveUserRolesUseCase;
import com.ecommerce.identity.application.usecase.admin.GetUserByIdUseCase;
import com.ecommerce.identity.application.usecase.admin.ListUsersUseCase;
import com.ecommerce.identity.application.usecase.admin.UpdateUserRolesUseCase;
import com.ecommerce.identity.application.usecase.admin.UpdateUserStatusUseCase;
import com.ecommerce.identity.application.usecase.agent.CreateAgentIdentityUseCase;
import com.ecommerce.identity.application.usecase.agent.GetAgentAuditLogsUseCase;
import com.ecommerce.identity.application.usecase.agent.GetAgentIdentityUseCase;
import com.ecommerce.identity.application.usecase.agent.ListMyAgentIdentitiesUseCase;
import com.ecommerce.identity.application.usecase.agent.RevokeAgentUseCase;
import com.ecommerce.identity.application.usecase.agent.SuspendAgentUseCase;
import com.ecommerce.identity.application.usecase.agent.UpdateAgentPermissionsUseCase;
import com.ecommerce.identity.application.usecase.consent.AgreePrivacyUseCase;
import com.ecommerce.identity.application.usecase.consent.AgreeTermsUseCase;
import com.ecommerce.identity.application.usecase.consent.GetMyConsentsUseCase;
import com.ecommerce.identity.application.usecase.consent.UpdateMarketingConsentUseCase;
import com.ecommerce.identity.application.usecase.kyc.ApproveKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.CancelKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.CreateKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.GetKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.ListMyKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.RejectKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.ReviewKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.SubmitKycUseCase;
import com.ecommerce.identity.application.usecase.kyc.UploadKycDocumentUseCase;
import com.ecommerce.identity.application.usecase.preference.UpdateAiPrivacyPreferenceUseCase;
import com.ecommerce.identity.application.usecase.preference.GetUserPreferenceUseCase;
import com.ecommerce.identity.application.usecase.preference.UpdateGeneralPreferenceUseCase;
import com.ecommerce.identity.application.usecase.preference.UpdateNotificationPreferenceUseCase;
import com.ecommerce.identity.application.usecase.registration.CompleteRegistrationUseCase;
import com.ecommerce.identity.application.usecase.registration.InitiateRegistrationUseCase;
import com.ecommerce.identity.application.usecase.registration.VerifyRegistrationOtpUseCase;
import com.ecommerce.identity.application.usecase.security.ChangePasswordUseCase;
import com.ecommerce.identity.application.usecase.security.CompletePasswordResetUseCase;
import com.ecommerce.identity.application.usecase.security.DisableMfaUseCase;
import com.ecommerce.identity.application.usecase.security.EnableMfaUseCase;
import com.ecommerce.identity.application.usecase.security.GetSecurityAuditLogsUseCase;
import com.ecommerce.identity.application.usecase.security.RegenerateBackupCodesUseCase;
import com.ecommerce.identity.application.usecase.security.RequestPasswordResetUseCase;
import com.ecommerce.identity.application.usecase.security.UnlockAccountUseCase;
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

    private final RegistrationService registrationService;
    private final IdentityRegistrationProperties registrationProperties;
    private final PreferenceService preferenceService;
    private final ConsentService consentService;
    private final KycService kycService;
    private final AddressService addressService;
    private final AdminUserService adminUserService;
    private final AgentService agentService;
    private final SecurityService securityService;

    @Bean
    public InitiateRegistrationInputPort initiateRegistrationUseCase() {
        return new InitiateRegistrationUseCase(registrationService);
    }

    @Bean
    public VerifyRegistrationOtpInputPort verifyRegistrationOtpUseCase() {
        return new VerifyRegistrationOtpUseCase(registrationService);
    }

    @Bean
    public CompleteRegistrationInputPort completeRegistrationUseCase() {
        return new CompleteRegistrationUseCase(registrationService);
    }

    @Bean
    public RegistrationPolicy registrationPolicy() {
        return new RegistrationPolicy(
                registrationProperties.getMaxVerifyAttempts(),
                registrationProperties.getResendCooldownSeconds(),
                registrationProperties.getOtpExpirySeconds(),
                registrationProperties.isExposeOtpInResponse());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordHasher passwordHasher(PasswordEncoder passwordEncoder) {
        return new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return passwordEncoder.encode(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, String hashedPassword) {
                return passwordEncoder.matches(rawPassword, hashedPassword);
            }
        };
    }

    @Bean
    public GetUserPreferenceQueryPort getUserPreferenceUseCase() {
        return new GetUserPreferenceUseCase(preferenceService);
    }

    @Bean
    public UpdateGeneralPreferenceInputPort updateGeneralPreferenceUseCase() {
        return new UpdateGeneralPreferenceUseCase(preferenceService);
    }

    @Bean
    public UpdateNotificationPreferenceInputPort updateNotificationPreferenceUseCase() {
        return new UpdateNotificationPreferenceUseCase(preferenceService);
    }

    @Bean
    public UpdateAiPrivacyPreferenceInputPort updateAiPrivacyPreferenceUseCase() {
        return new UpdateAiPrivacyPreferenceUseCase(preferenceService);
    }

    @Bean
    public GetMyConsentsQueryPort getMyConsentsUseCase() {
        return new GetMyConsentsUseCase(consentService);
    }

    @Bean
    public AgreeTermsInputPort agreeTermsUseCase() {
        return new AgreeTermsUseCase(consentService);
    }

    @Bean
    public AgreePrivacyInputPort agreePrivacyUseCase() {
        return new AgreePrivacyUseCase(consentService);
    }

    @Bean
    public UpdateMarketingConsentInputPort updateMarketingConsentUseCase() {
        return new UpdateMarketingConsentUseCase(consentService);
    }

    @Bean
    public ListMyKycQueryPort listMyKycUseCase() {
        return new ListMyKycUseCase(kycService);
    }

    @Bean
    public GetKycQueryPort getKycUseCase() {
        return new GetKycUseCase(kycService);
    }

    @Bean
    public CreateKycInputPort createKycUseCase() {
        return new CreateKycUseCase(kycService);
    }

    @Bean
    public UploadKycDocumentInputPort uploadKycDocumentUseCase() {
        return new UploadKycDocumentUseCase(kycService);
    }

    @Bean
    public SubmitKycInputPort submitKycUseCase() {
        return new SubmitKycUseCase(kycService);
    }

    @Bean
    public CancelKycInputPort cancelKycUseCase() {
        return new CancelKycUseCase(kycService);
    }

    @Bean
    public ReviewKycInputPort reviewKycUseCase() {
        return new ReviewKycUseCase(kycService);
    }

    @Bean
    public ApproveKycInputPort approveKycUseCase() {
        return new ApproveKycUseCase(kycService);
    }

    @Bean
    public RejectKycInputPort rejectKycUseCase() {
        return new RejectKycUseCase(kycService);
    }

    @Bean
    public ListUsersQueryPort listUsersUseCase() {
        return new ListUsersUseCase(adminUserService);
    }

    @Bean
    public GetUserByIdQueryPort getUserByIdUseCase() {
        return new GetUserByIdUseCase(adminUserService);
    }

    @Bean
    public UpdateUserRolesInputPort updateUserRolesUseCase() {
        return new UpdateUserRolesUseCase(adminUserService);
    }

    @Bean
    public RemoveUserRolesInputPort removeUserRolesUseCase() {
        return new RemoveUserRolesUseCase(adminUserService);
    }

    @Bean
    public UpdateUserStatusInputPort updateUserStatusUseCase() {
        return new UpdateUserStatusUseCase(adminUserService);
    }

    @Bean
    public ListMyAgentIdentitiesQueryPort listMyAgentIdentitiesUseCase() {
        return new ListMyAgentIdentitiesUseCase(agentService);
    }

    @Bean
    public GetAgentIdentityQueryPort getAgentIdentityUseCase() {
        return new GetAgentIdentityUseCase(agentService);
    }

    @Bean
    public CreateAgentIdentityInputPort createAgentIdentityUseCase() {
        return new CreateAgentIdentityUseCase(agentService);
    }

    @Bean
    public UpdateAgentPermissionsInputPort updateAgentPermissionsUseCase() {
        return new UpdateAgentPermissionsUseCase(agentService);
    }

    @Bean
    public SuspendAgentInputPort suspendAgentUseCase() {
        return new SuspendAgentUseCase(agentService);
    }

    @Bean
    public GetAgentAuditLogsQueryPort getAgentAuditLogsUseCase() {
        return new GetAgentAuditLogsUseCase(agentService);
    }

    @Bean
    public RevokeAgentInputPort revokeAgentUseCase() {
        return new RevokeAgentUseCase(agentService);
    }

    @Bean
    public ChangePasswordInputPort changePasswordUseCase() {
        return new ChangePasswordUseCase(securityService);
    }

    @Bean
    public RequestPasswordResetInputPort requestPasswordResetUseCase() {
        return new RequestPasswordResetUseCase(securityService);
    }

    @Bean
    public CompletePasswordResetInputPort completePasswordResetUseCase() {
        return new CompletePasswordResetUseCase(securityService);
    }

    @Bean
    public EnableMfaInputPort enableMfaUseCase() {
        return new EnableMfaUseCase(securityService);
    }

    @Bean
    public DisableMfaInputPort disableMfaUseCase() {
        return new DisableMfaUseCase(securityService);
    }

    @Bean
    public RegenerateBackupCodesInputPort regenerateBackupCodesUseCase() {
        return new RegenerateBackupCodesUseCase(securityService);
    }

    @Bean
    public GetSecurityAuditLogsQueryPort getSecurityAuditLogsUseCase() {
        return new GetSecurityAuditLogsUseCase(securityService);
    }

    @Bean
    public UnlockAccountInputPort unlockAccountUseCase() {
        return new UnlockAccountUseCase(adminUserService);
    }
}
