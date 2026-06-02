package com.aionn.identity.adapter.rest.controller;

import com.aionn.identity.adapter.rest.dto.auth.response.AuthTokenResponse;
import com.aionn.identity.adapter.rest.dto.registration.request.CompleteRegistrationRequest;
import com.aionn.identity.adapter.rest.dto.registration.request.InitiateRegistrationRequest;
import com.aionn.identity.adapter.rest.dto.registration.request.VerifyOtpRequest;
import com.aionn.identity.adapter.rest.dto.registration.response.RegistrationSessionResponse;
import com.aionn.identity.adapter.rest.dto.registration.response.VerifyOtpResponse;
import com.aionn.identity.adapter.rest.exception.IdentityExceptionHandler;
import com.aionn.identity.adapter.rest.mapper.registration.RegistrationDtoMapper;
import com.aionn.identity.adapter.rest.support.client.AuthClientTypeArgumentResolver;
import com.aionn.identity.adapter.rest.support.client.ClientUserAgentArgumentResolver;
import com.aionn.identity.adapter.rest.support.response.AuthTokenResponseHandler;
import com.aionn.identity.application.dto.registration.command.CompleteRegistrationCommand;
import com.aionn.identity.application.dto.registration.command.InitiateRegistrationCommand;
import com.aionn.identity.application.dto.registration.command.ResendRegistrationOtpCommand;
import com.aionn.identity.application.dto.registration.command.VerifyRegistrationOtpCommand;
import com.aionn.identity.application.dto.registration.result.CompleteRegistrationResult;
import com.aionn.identity.application.dto.registration.result.InitiateRegistrationResult;
import com.aionn.identity.application.dto.registration.result.ResendRegistrationOtpResult;
import com.aionn.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import com.aionn.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.aionn.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.aionn.identity.application.port.in.registration.ResendRegistrationOtpInputPort;
import com.aionn.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.infrastructure.config.properties.AuthProperties;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import com.aionn.sharedkernel.adapter.web.support.clientip.ClientIpArgumentResolver;
import com.aionn.sharedkernel.infrastructure.web.ClientIpResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerWebTest {

        @Mock
        private InitiateRegistrationInputPort initiateRegistrationInputPort;
        @Mock
        private VerifyRegistrationOtpInputPort verifyRegistrationOtpInputPort;
        @Mock
        private CompleteRegistrationInputPort completeRegistrationInputPort;
        @Mock
        private ResendRegistrationOtpInputPort resendRegistrationOtpInputPort;
        @Mock
        private RegistrationDtoMapper registrationDtoMapper;
        @Mock
        private AuthTokenResponseHandler authTokenResponseHandler;
        @Mock
        private AuthProperties authProperties;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                RegistrationController controller = new RegistrationController(
                                initiateRegistrationInputPort,
                                verifyRegistrationOtpInputPort,
                                completeRegistrationInputPort,
                                resendRegistrationOtpInputPort,
                                registrationDtoMapper,
                                authTokenResponseHandler);

                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new IdentityExceptionHandler())
                                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                                                Jackson2ObjectMapperBuilder.json().build()))
                                .setCustomArgumentResolvers(
                                                new ClientIpArgumentResolver(new ClientIpResolver()),
                                                new ClientUserAgentArgumentResolver(),
                                                new AuthClientTypeArgumentResolver(authProperties))
                                .build();
        }

        @Test
        void initiateRegistrationResolvesClientIpAndReturnsCreatedPayload() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                InitiateRegistrationResult result = new InitiateRegistrationResult(
                                "reg-1",
                                now.plusSeconds(60),
                                now.plusMinutes(5),
                                "123456");
                RegistrationSessionResponse response = new RegistrationSessionResponse(
                                "reg-1",
                                result.resendAvailableAt(),
                                result.expiredAt(),
                                result.otpCode());

                when(registrationDtoMapper.toInitiateCommand(any(InitiateRegistrationRequest.class),
                                eq("203.0.113.10")))
                                .thenReturn(new InitiateRegistrationCommand("0912345678", "captcha-ok",
                                                "203.0.113.10"));
                when(initiateRegistrationInputPort.execute(any())).thenReturn(result);
                when(registrationDtoMapper.toInitiateResponse(result)).thenReturn(response);

                mockMvc.perform(post("/api/v1/registrations/initiate")
                                .contentType(APPLICATION_JSON)
                                .header("X-Forwarded-For", "203.0.113.10")
                                .content("""
                                                {
                                                  "phoneNumber": "0912345678",
                                                  "captchaToken": "captcha-ok"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.statusCode").value("201"))
                                .andExpect(jsonPath("$.message").value("Registration initiated successfully!"))
                                .andExpect(jsonPath("$.data.regId").value("reg-1"))
                                .andExpect(jsonPath("$.data.otpCode").value("123456"));

                verify(registrationDtoMapper).toInitiateCommand(
                                eq(new InitiateRegistrationRequest("0912345678", "captcha-ok")),
                                eq("203.0.113.10"));
        }

        @Test
        void completeRegistrationResolvesClientContextBeforeDelegating() throws Exception {
                when(authProperties.clientTypeHeader()).thenReturn("X-Client-Type");
                LocalDateTime now = LocalDateTime.now();
                CompleteRegistrationResult result = new CompleteRegistrationResult(
                                "user-1",
                                "session-1",
                                "refresh-1",
                                "access-1",
                                now.plusMinutes(15),
                                now.plusDays(7));
                AuthTokenResponse authTokenResponse = new AuthTokenResponse(
                                result.userId(),
                                result.sessionId(),
                                result.refreshToken(),
                                result.accessToken(),
                                result.expiresAt(),
                                result.sessionExpiresAt());
                ResponseEntity<ApiResponse<AuthTokenResponse>> httpResponse = ResponseEntity.ok(
                                ApiResponse.success(authTokenResponse, "Registration completed!"));

                when(registrationDtoMapper.toCompleteCommand(
                                eq("reg-1"),
                                any(CompleteRegistrationRequest.class),
                                eq("198.51.100.20"),
                                eq("JUnit/1.0")))
                                .thenReturn(new CompleteRegistrationCommand(
                                                "reg-1",
                                                "Password1!",
                                                "alice",
                                                "verify-token",
                                                "198.51.100.20",
                                                "JUnit/1.0"));
                when(completeRegistrationInputPort.execute(any())).thenReturn(result);
                when(registrationDtoMapper.toAuthTokenResponse(result)).thenReturn(authTokenResponse);
                when(authTokenResponseHandler.success(authTokenResponse, "mobile", "Registration completed!"))
                                .thenReturn(httpResponse);

                mockMvc.perform(post("/api/v1/registrations/reg-1/complete")
                                .contentType(APPLICATION_JSON)
                                .header("X-Forwarded-For", "198.51.100.20")
                                .header("User-Agent", "JUnit/1.0")
                                .header("X-Client-Type", "mobile")
                                .content("""
                                                {
                                                  "password": "Password1!",
                                                  "username": "alice",
                                                  "verificationToken": "verify-token"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value("200"))
                                .andExpect(jsonPath("$.message").value("Registration completed!"))
                                .andExpect(jsonPath("$.data.userId").value("user-1"))
                                .andExpect(jsonPath("$.data.refreshToken").value("refresh-1"));

                verify(registrationDtoMapper).toCompleteCommand(
                                eq("reg-1"),
                                eq(new CompleteRegistrationRequest("Password1!", "alice", "verify-token")),
                                eq("198.51.100.20"),
                                eq("JUnit/1.0"));
                verify(authTokenResponseHandler).success(authTokenResponse, "mobile", "Registration completed!");
        }

        @Test
        void verifyOtpSuccessfullyVerifiesAndReturnsToken() throws Exception {
                VerifyRegistrationOtpResult result = new VerifyRegistrationOtpResult("reg-1", "verify-token-abc");
                VerifyOtpResponse response = new VerifyOtpResponse("reg-1", "verify-token-abc");

                when(registrationDtoMapper.toVerifyOtpCommand("reg-1", new VerifyOtpRequest("123456")))
                                .thenReturn(new VerifyRegistrationOtpCommand("reg-1", "123456"));
                when(verifyRegistrationOtpInputPort.execute(any())).thenReturn(result);
                when(registrationDtoMapper.toVerifyOtpResponse(result)).thenReturn(response);

                mockMvc.perform(post("/api/v1/registrations/reg-1/verify-otp")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "otpCode": "123456"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("OTP verified successfully!"))
                                .andExpect(jsonPath("$.data.regId").value("reg-1"))
                                .andExpect(jsonPath("$.data.verificationToken").value("verify-token-abc"));

                verify(verifyRegistrationOtpInputPort).execute(any());
        }

        @Test
        void resendOtpSuccessfullyResendsAndReturnsSession() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                ResendRegistrationOtpResult result = new ResendRegistrationOtpResult(
                                "reg-1", now.plusSeconds(60), now.plusMinutes(5), "654321");
                RegistrationSessionResponse response = new RegistrationSessionResponse(
                                "reg-1", result.resendAvailableAt(), result.expiredAt(), result.otpCode());

                when(registrationDtoMapper.toResendOtpCommand("reg-1", "203.0.113.50"))
                                .thenReturn(new ResendRegistrationOtpCommand("reg-1", "203.0.113.50"));
                when(resendRegistrationOtpInputPort.execute(any())).thenReturn(result);
                when(registrationDtoMapper.toResendOtpResponse(result)).thenReturn(response);

                mockMvc.perform(post("/api/v1/registrations/reg-1/resend-otp")
                                .header("X-Forwarded-For", "203.0.113.50"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("OTP resent successfully!"))
                                .andExpect(jsonPath("$.data.regId").value("reg-1"))
                                .andExpect(jsonPath("$.data.otpCode").value("654321"));

                verify(resendRegistrationOtpInputPort).execute(any());
        }

        @Test
        void initiateRegistrationRejectsInvalidPhoneFormat() throws Exception {
                mockMvc.perform(post("/api/v1/registrations/initiate")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "phoneNumber": "123",
                                                  "captchaToken": "captcha-ok"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

                verifyNoInteractions(initiateRegistrationInputPort);
        }

        @Test
        void initiateRegistrationRejectsBlankCaptcha() throws Exception {
                mockMvc.perform(post("/api/v1/registrations/initiate")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "phoneNumber": "0912345678",
                                                  "captchaToken": ""
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

                verifyNoInteractions(initiateRegistrationInputPort);
        }

        @Test
        void verifyOtpRejectsNonSixDigitOtp() throws Exception {
                mockMvc.perform(post("/api/v1/registrations/reg-1/verify-otp")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "otpCode": "12345"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

                verifyNoInteractions(verifyRegistrationOtpInputPort);
        }

        @Test
        void completeRegistrationRejectsWeakPassword() throws Exception {
                mockMvc.perform(post("/api/v1/registrations/reg-1/complete")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "password": "weak",
                                                  "username": "alice",
                                                  "verificationToken": "verify-token"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));

                verifyNoInteractions(completeRegistrationInputPort);
        }

        @Test
        void initiateRegistrationWhenPhoneExistsReturns409() throws Exception {
                when(registrationDtoMapper.toInitiateCommand(any(InitiateRegistrationRequest.class), eq("127.0.0.1")))
                                .thenReturn(new InitiateRegistrationCommand("0912345678", "captcha-ok", "127.0.0.1"));
                when(initiateRegistrationInputPort.execute(any()))
                                .thenThrow(new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS));

                mockMvc.perform(post("/api/v1/registrations/initiate")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "phoneNumber": "0912345678",
                                                  "captchaToken": "captcha-ok"
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.data.errorCode").value("IDENTITY_001"));
        }

        @Test
        void verifyOtpWhenSessionNotFoundReturns404() throws Exception {
                when(registrationDtoMapper.toVerifyOtpCommand("reg-unknown", new VerifyOtpRequest("123456")))
                                .thenReturn(new VerifyRegistrationOtpCommand("reg-unknown", "123456"));
                when(verifyRegistrationOtpInputPort.execute(any()))
                                .thenThrow(new IdentityException(IdentityErrorCode.REGISTRATION_SESSION_NOT_FOUND));

                mockMvc.perform(post("/api/v1/registrations/reg-unknown/verify-otp")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "otpCode": "123456"
                                                }
                                                """))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.data.errorCode").value("IDENTITY_109"));
        }

        @Test
        void verifyOtpWhenOtpInvalidReturns400() throws Exception {
                when(registrationDtoMapper.toVerifyOtpCommand("reg-1", new VerifyOtpRequest("999999")))
                                .thenReturn(new VerifyRegistrationOtpCommand("reg-1", "999999"));
                when(verifyRegistrationOtpInputPort.execute(any()))
                                .thenThrow(new IdentityException(IdentityErrorCode.OTP_INVALID));

                mockMvc.perform(post("/api/v1/registrations/reg-1/verify-otp")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "otpCode": "999999"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.data.errorCode").value("IDENTITY_101"));
        }

        @Test
        void resendOtpWhenRateLimitExceededReturns429() throws Exception {
                when(registrationDtoMapper.toResendOtpCommand("reg-1", "127.0.0.1"))
                                .thenReturn(new ResendRegistrationOtpCommand("reg-1", "127.0.0.1"));
                when(resendRegistrationOtpInputPort.execute(any()))
                                .thenThrow(new IdentityException(IdentityErrorCode.RATE_LIMIT_EXCEEDED));

                mockMvc.perform(post("/api/v1/registrations/reg-1/resend-otp"))
                                .andExpect(status().isTooManyRequests())
                                .andExpect(jsonPath("$.data.errorCode").value("IDENTITY_107"));
        }
}
