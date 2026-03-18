package com.ecommerce.identity.presentation.controller;

import com.ecommerce.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.ecommerce.identity.presentation.dto.registration.*;
import com.ecommerce.identity.presentation.mapper.registration.RegistrationDtoMapper;
import com.ecommerce.sharedkernel.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationController {

        private final InitiateRegistrationInputPort initiateRegistrationInputPort;
        private final VerifyRegistrationOtpInputPort verifyRegistrationOtpInputPort;
        private final CompleteRegistrationInputPort completeRegistrationInputPort;
        private final RegistrationDtoMapper registrationDtoMapper;

        @PostMapping("/initiate")
        public ResponseEntity<ApiResponse<InitiateRegistrationResponse>> initRegistration(
                        @Valid @RequestBody InitiateRegistrationRequest request,
                        HttpServletRequest httpServletRequest) {
                String clientIp = extractClientIp(httpServletRequest);
                var result = initiateRegistrationInputPort
                                .execute(registrationDtoMapper.toInitiateCommand(request, clientIp));
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(registrationDtoMapper.toInitiateResponse(result),
                                                "Registration initiated"));
        }

        @PostMapping("/{regId}/verify-otp")
        public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
                        @PathVariable("regId") String regId,
                        @Valid @RequestBody VerifyOtpRequest request) {
                var result = verifyRegistrationOtpInputPort
                                .execute(registrationDtoMapper.toVerifyOtpCommand(regId, request));
                return ResponseEntity
                                .ok(ApiResponse.success(registrationDtoMapper.toVerifyOtpResponse(result),
                                                "OTP verified"));
        }

        @PostMapping("/{regId}/complete")
        public ResponseEntity<ApiResponse<CompleteRegistrationResponse>> completeRegistration(
                        @PathVariable("regId") String regId,
                        @Valid @RequestBody CompleteRegistrationRequest request) {
                var result = completeRegistrationInputPort
                                .execute(registrationDtoMapper.toCompleteCommand(regId, request));
                return ResponseEntity
                                .ok(ApiResponse.success(registrationDtoMapper.toCompleteResponse(result),
                                                "Registration completed"));
        }

        private String extractClientIp(HttpServletRequest request) {
                String forwardedFor = request.getHeader("X-Forwarded-For");
                if (forwardedFor != null && !forwardedFor.isBlank()) {
                        return forwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
        }
}
