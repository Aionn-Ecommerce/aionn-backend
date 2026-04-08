package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.registration.result.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.ResendRegistrationOtpResult;
import com.ecommerce.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.RegistrationVerificationSession;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RegistrationResultMapperImpl implements RegistrationResultMapper {

    @Override
    public InitiateRegistrationResult toInitiateResult(RegistrationVerificationSession session, String otpCode) {
        if ( session == null && otpCode == null ) {
            return null;
        }

        String regId = null;
        LocalDateTime resendAvailableAt = null;
        LocalDateTime expiredAt = null;
        String otpCode1 = null;
        if ( session != null ) {
            regId = session.getRegId();
            resendAvailableAt = session.getResendAvailableAt();
            expiredAt = session.getExpiredAt();
            otpCode1 = session.getOtpCode();
        }

        InitiateRegistrationResult initiateRegistrationResult = new InitiateRegistrationResult( regId, resendAvailableAt, expiredAt, otpCode1 );

        return initiateRegistrationResult;
    }

    @Override
    public VerifyRegistrationOtpResult toVerifyOtpResult(String regId, String verificationToken) {
        if ( regId == null && verificationToken == null ) {
            return null;
        }

        String regId1 = null;
        regId1 = regId;
        String verificationToken1 = null;
        verificationToken1 = verificationToken;

        VerifyRegistrationOtpResult verifyRegistrationOtpResult = new VerifyRegistrationOtpResult( regId1, verificationToken1 );

        return verifyRegistrationOtpResult;
    }

    @Override
    public ResendRegistrationOtpResult toResendOtpResult(RegistrationVerificationSession session, String otpCode) {
        if ( session == null && otpCode == null ) {
            return null;
        }

        String regId = null;
        LocalDateTime resendAvailableAt = null;
        LocalDateTime expiredAt = null;
        String otpCode1 = null;
        if ( session != null ) {
            regId = session.getRegId();
            resendAvailableAt = session.getResendAvailableAt();
            expiredAt = session.getExpiredAt();
            otpCode1 = session.getOtpCode();
        }

        ResendRegistrationOtpResult resendRegistrationOtpResult = new ResendRegistrationOtpResult( regId, resendAvailableAt, expiredAt, otpCode1 );

        return resendRegistrationOtpResult;
    }

    @Override
    public CompleteRegistrationResult toCompleteResult(AuthSession session, String accessToken) {
        if ( session == null && accessToken == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        String refreshToken = null;
        LocalDateTime expiresAt = null;
        if ( session != null ) {
            userId = session.getUserId();
            sessionId = session.getSessionId();
            refreshToken = session.getSessionId();
            expiresAt = session.getExpiresAt();
        }
        String accessToken1 = null;
        accessToken1 = accessToken;

        CompleteRegistrationResult completeRegistrationResult = new CompleteRegistrationResult( userId, sessionId, refreshToken, accessToken1, expiresAt );

        return completeRegistrationResult;
    }
}
