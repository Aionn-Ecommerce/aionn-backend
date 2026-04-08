package com.ecommerce.identity.adapter.rest.mapper.registration;

import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.adapter.rest.dto.registration.CompleteRegistrationRequest;
import com.ecommerce.identity.adapter.rest.dto.registration.InitiateRegistrationRequest;
import com.ecommerce.identity.adapter.rest.dto.registration.RegistrationSessionResponse;
import com.ecommerce.identity.adapter.rest.dto.registration.VerifyOtpRequest;
import com.ecommerce.identity.adapter.rest.dto.registration.VerifyOtpResponse;
import com.ecommerce.identity.application.dto.registration.command.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.command.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.command.ResendRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.command.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.result.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.ResendRegistrationOtpResult;
import com.ecommerce.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RegistrationDtoMapperImpl implements RegistrationDtoMapper {

    @Override
    public InitiateRegistrationCommand toInitiateCommand(InitiateRegistrationRequest request, String ipAddress) {
        if ( request == null && ipAddress == null ) {
            return null;
        }

        String identity = null;
        String captchaToken = null;
        if ( request != null ) {
            identity = request.phoneNumber();
            captchaToken = request.captchaToken();
        }
        String ipAddress1 = null;
        ipAddress1 = ipAddress;

        InitiateRegistrationCommand initiateRegistrationCommand = new InitiateRegistrationCommand( identity, captchaToken, ipAddress1 );

        return initiateRegistrationCommand;
    }

    @Override
    public RegistrationSessionResponse toInitiateResponse(InitiateRegistrationResult result) {
        if ( result == null ) {
            return null;
        }

        String regId = null;
        LocalDateTime resendAvailableAt = null;
        LocalDateTime expiredAt = null;
        String otpCode = null;

        regId = result.regId();
        resendAvailableAt = result.resendAvailableAt();
        expiredAt = result.expiredAt();
        otpCode = result.otpCode();

        RegistrationSessionResponse registrationSessionResponse = new RegistrationSessionResponse( regId, resendAvailableAt, expiredAt, otpCode );

        return registrationSessionResponse;
    }

    @Override
    public VerifyRegistrationOtpCommand toVerifyOtpCommand(String regId, VerifyOtpRequest request) {
        if ( regId == null && request == null ) {
            return null;
        }

        String otpCode = null;
        if ( request != null ) {
            otpCode = request.otpCode();
        }
        String regId1 = null;
        regId1 = regId;

        VerifyRegistrationOtpCommand verifyRegistrationOtpCommand = new VerifyRegistrationOtpCommand( regId1, otpCode );

        return verifyRegistrationOtpCommand;
    }

    @Override
    public VerifyOtpResponse toVerifyOtpResponse(VerifyRegistrationOtpResult result) {
        if ( result == null ) {
            return null;
        }

        String regId = null;
        String verificationToken = null;

        regId = result.regId();
        verificationToken = result.verificationToken();

        VerifyOtpResponse verifyOtpResponse = new VerifyOtpResponse( regId, verificationToken );

        return verifyOtpResponse;
    }

    @Override
    public CompleteRegistrationCommand toCompleteCommand(String regId, CompleteRegistrationRequest request, String ipAddress, String userAgent) {
        if ( regId == null && request == null && ipAddress == null && userAgent == null ) {
            return null;
        }

        String password = null;
        String username = null;
        String verificationToken = null;
        if ( request != null ) {
            password = request.password();
            username = request.username();
            verificationToken = request.verificationToken();
        }
        String regId1 = null;
        regId1 = regId;
        String ipAddress1 = null;
        ipAddress1 = ipAddress;
        String userAgent1 = null;
        userAgent1 = userAgent;

        CompleteRegistrationCommand completeRegistrationCommand = new CompleteRegistrationCommand( regId1, password, username, verificationToken, ipAddress1, userAgent1 );

        return completeRegistrationCommand;
    }

    @Override
    public AuthTokenResponse toAuthTokenResponse(CompleteRegistrationResult result) {
        if ( result == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        String refreshToken = null;
        String accessToken = null;
        LocalDateTime expiresAt = null;

        userId = result.userId();
        sessionId = result.sessionId();
        refreshToken = result.refreshToken();
        accessToken = result.accessToken();
        expiresAt = result.expiresAt();

        AuthTokenResponse authTokenResponse = new AuthTokenResponse( userId, sessionId, refreshToken, accessToken, expiresAt );

        return authTokenResponse;
    }

    @Override
    public ResendRegistrationOtpCommand toResendOtpCommand(String regId, String ipAddress) {
        if ( regId == null && ipAddress == null ) {
            return null;
        }

        String regId1 = null;
        regId1 = regId;
        String ipAddress1 = null;
        ipAddress1 = ipAddress;

        ResendRegistrationOtpCommand resendRegistrationOtpCommand = new ResendRegistrationOtpCommand( regId1, ipAddress1 );

        return resendRegistrationOtpCommand;
    }

    @Override
    public RegistrationSessionResponse toResendOtpResponse(ResendRegistrationOtpResult result) {
        if ( result == null ) {
            return null;
        }

        String regId = null;
        LocalDateTime resendAvailableAt = null;
        LocalDateTime expiredAt = null;
        String otpCode = null;

        regId = result.regId();
        resendAvailableAt = result.resendAvailableAt();
        expiredAt = result.expiredAt();
        otpCode = result.otpCode();

        RegistrationSessionResponse registrationSessionResponse = new RegistrationSessionResponse( regId, resendAvailableAt, expiredAt, otpCode );

        return registrationSessionResponse;
    }
}
