package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.registration.*;
import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.adapter.rest.mapper.registration.RegistrationDtoMapper;
import com.ecommerce.sharedkernel.adapter.web.support.ClientIp;
import com.ecommerce.identity.application.port.in.registration.CompleteRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.ecommerce.identity.application.port.in.registration.VerifyRegistrationOtpInputPort;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Tag(name = "Identity - Registration", description = "Identity module: registration and OTP verification endpoints")
public class RegistrationController {

	private final InitiateRegistrationInputPort initiateRegistrationInputPort;
	private final VerifyRegistrationOtpInputPort verifyRegistrationOtpInputPort;
	private final CompleteRegistrationInputPort completeRegistrationInputPort;
	private final RegistrationDtoMapper registrationDtoMapper;

	@Operation(summary = "Initiate registration", description = "Start registration and send OTP to user")
	@PostMapping("/initiate")
	public ResponseEntity<ApiResponse<InitiateRegistrationResponse>> initRegistration(
			@Valid @RequestBody InitiateRegistrationRequest request,
			@ClientIp String clientIp) {
		var result = initiateRegistrationInputPort.execute(registrationDtoMapper.toInitiateCommand(request, clientIp));
		InitiateRegistrationResponse response = registrationDtoMapper.toInitiateResponse(result);
		return ApiResponse.createdResponse("Registration initiated successfully!", response);
	}

	@Operation(summary = "Verify OTP", description = "Verify OTP for a registration session")
	@PostMapping("/{regId}/verify-otp")
	public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
			@PathVariable String regId,
			@Valid @RequestBody VerifyOtpRequest request) {
		var result = verifyRegistrationOtpInputPort.execute(registrationDtoMapper.toVerifyOtpCommand(regId, request));
		VerifyOtpResponse response = registrationDtoMapper.toVerifyOtpResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully!"));
	}

	@Operation(summary = "Complete registration", description = "Finalize registration and create user account")
	@PostMapping("/{regId}/complete")
	public ResponseEntity<ApiResponse<AuthTokenResponse>> completeRegistration(
			@PathVariable String regId,
			@Valid @RequestBody CompleteRegistrationRequest request) {
		var result = completeRegistrationInputPort.execute(registrationDtoMapper.toCompleteCommand(regId, request));
		AuthTokenResponse response = registrationDtoMapper.toAuthTokenResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Registration completed!"));
	}
}
