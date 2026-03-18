package com.ecommerce.identity.presentation.controller;

import com.ecommerce.identity.application.dto.registration.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpResult;
import com.ecommerce.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.ecommerce.identity.presentation.dto.registration.CompleteRegistrationRequest;
import com.ecommerce.identity.presentation.dto.registration.CompleteRegistrationResponse;
import com.ecommerce.identity.presentation.dto.registration.InitiateRegistrationRequest;
import com.ecommerce.identity.presentation.dto.registration.InitiateRegistrationResponse;
import com.ecommerce.identity.presentation.dto.registration.VerifyOtpRequest;
import com.ecommerce.identity.presentation.dto.registration.VerifyOtpResponse;
import com.ecommerce.identity.presentation.mapper.registration.RegistrationDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

        @Mock
        private InitiateRegistrationInputPort initiateRegistrationUseCase;

        @Mock
        private VerifyRegistrationOtpInputPort verifyRegistrationOtpUseCase;

        @Mock
        private CompleteRegistrationInputPort completeRegistrationUseCase;

        @Mock
        private RegistrationDtoMapper registrationDtoMapper;

        @InjectMocks
        private RegistrationController registrationController;

        private MockMvc mockMvc() {
                return MockMvcBuilders.standaloneSetup(registrationController).build();
        }

        @Test
        void shouldReturn201WhenStartRegistrationSuccess() throws Exception {
                Mockito.when(registrationDtoMapper.toInitiateCommand(
                                Mockito.any(InitiateRegistrationRequest.class),
                                Mockito.anyString()))
                                .thenReturn(new InitiateRegistrationCommand("0987654321", "captcha-token",
                                                "127.0.0.1"));
                Mockito.when(initiateRegistrationUseCase.execute(Mockito.any()))
                                .thenReturn(new InitiateRegistrationResult(
                                                "reg-123",
                                                LocalDateTime.of(2026, 3, 14, 10, 5, 0),
                                                LocalDateTime.of(2026, 3, 14, 10, 10, 0),
                                                "123456"));
                Mockito.when(registrationDtoMapper.toInitiateResponse(Mockito.any(InitiateRegistrationResult.class)))
                                .thenReturn(new InitiateRegistrationResponse(
                                                "reg-123",
                                                LocalDateTime.of(2026, 3, 14, 10, 5, 0),
                                                LocalDateTime.of(2026, 3, 14, 10, 10, 0),
                                                "123456"));

                mockMvc().perform(post("/api/v1/registrations/initiate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                                                                                                                                                                        "phoneNumber": "0987654321",
                                                                                                                                                                                                        "captchaToken": "captcha-token"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(content().string(containsString("reg-123")));
        }

        @Test
        void shouldReturn200WhenVerifyOtpSuccess() throws Exception {
                Mockito.when(registrationDtoMapper.toVerifyOtpCommand(Mockito.eq("reg-123"),
                                Mockito.any(VerifyOtpRequest.class)))
                                .thenReturn(new VerifyRegistrationOtpCommand("reg-123", "123456"));
                Mockito.when(verifyRegistrationOtpUseCase.execute(Mockito.any()))
                                .thenReturn(new VerifyRegistrationOtpResult("reg-123", "verify-token-123"));
                Mockito.when(registrationDtoMapper.toVerifyOtpResponse(Mockito.any(VerifyRegistrationOtpResult.class)))
                                .thenReturn(new VerifyOtpResponse("reg-123", "verify-token-123"));

                mockMvc().perform(post("/api/v1/registrations/reg-123/verify-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "otpCode": "123456"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("reg-123")))
                                .andExpect(content().string(containsString("verify-token-123")));
        }

        @Test
        void shouldReturn200WhenCompleteRegistrationSuccess() throws Exception {
                Mockito.when(registrationDtoMapper.toCompleteCommand(Mockito.eq("reg-123"),
                                Mockito.any(CompleteRegistrationRequest.class)))
                                .thenReturn(new CompleteRegistrationCommand("reg-123", "Password@123", "tester",
                                                "verify-token-123"));
                Mockito.when(completeRegistrationUseCase.execute(Mockito.any()))
                                .thenReturn(new CompleteRegistrationResult(
                                                "u-123",
                                                "tester",
                                                LocalDateTime.of(2026, 3, 14, 10, 10, 0)));
                Mockito.when(registrationDtoMapper.toCompleteResponse(Mockito.any(CompleteRegistrationResult.class)))
                                .thenReturn(new CompleteRegistrationResponse(
                                                "u-123",
                                                "tester",
                                                LocalDateTime.of(2026, 3, 14, 10, 10, 0)));

                mockMvc().perform(post("/api/v1/registrations/reg-123/complete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                                                                                                                                                                        "password": "Password@123",
                                                                                                                                                                                                        "username": "tester",
                                                                                                                                                                                                        "verificationToken": "verify-token-123"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("u-123")));
        }
}
