package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.registration.result.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.ResendRegistrationOtpResult;
import com.ecommerce.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.RegistrationVerificationSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegistrationResultMapper {

    InitiateRegistrationResult toInitiateResult(RegistrationVerificationSession session, String otpCode);

    VerifyRegistrationOtpResult toVerifyOtpResult(String regId, String verificationToken);

    ResendRegistrationOtpResult toResendOtpResult(RegistrationVerificationSession session, String otpCode);

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "refreshToken", source = "session.sessionId")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    CompleteRegistrationResult toCompleteResult(AuthSession session, String accessToken);
}
