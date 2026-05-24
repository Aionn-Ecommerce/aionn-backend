package com.aionn.identity.adapter.rest.controller;

import com.aionn.identity.adapter.rest.dto.kyc.CreateKycRequest;
import com.aionn.identity.adapter.rest.dto.kyc.KycResponse;
import com.aionn.identity.adapter.rest.dto.kyc.KycVerificationSessionResponse;
import com.aionn.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.aionn.identity.application.port.in.kyc.CreateKycInputPort;
import com.aionn.identity.application.port.in.kyc.GenerateKycVerificationSessionInputPort;
import com.aionn.identity.application.port.in.kyc.GetKycQueryPort;
import com.aionn.identity.application.port.in.kyc.ListMyKycQueryPort;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "Identity - KYC", description = "Identity module: Sumsub-backed KYC session and status endpoints")
public class KycController {

	private final ListMyKycQueryPort listMyKycQueryPort;
	private final GetKycQueryPort getKycQueryPort;
	private final CreateKycInputPort createKycInputPort;
	private final GenerateKycVerificationSessionInputPort generateKycVerificationSessionInputPort;
	private final KycDtoMapper kycDtoMapper;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List my KYC profiles", description = "Get KYC profiles belonging to the authenticated user")
	public ResponseEntity<ApiResponse<List<KycResponse>>> listMyKyc(Authentication authentication) {
		var result = listMyKycQueryPort.execute(authentication.getName());
		var response = kycDtoMapper.toResponses(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC profiles fetched"));
	}

	@GetMapping("/{kycId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get KYC profile", description = "Get one KYC profile by KYC ID for the authenticated user")
	public ResponseEntity<ApiResponse<KycResponse>> getKyc(
			Authentication authentication,
			@PathVariable String kycId) {
		var result = getKycQueryPort.execute(kycDtoMapper.toGetKycQuery(authentication.getName(), kycId));
		var response = kycDtoMapper.toResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC profile fetched"));
	}

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Create KYC profile", description = "Create a new KYC profile for the authenticated user")
	public ResponseEntity<ApiResponse<KycResponse>> createKyc(
			Authentication authentication,
			@Valid @RequestBody CreateKycRequest request) {
		var result = createKycInputPort.execute(kycDtoMapper.toCreateKycCommand(authentication.getName(), request));
		var response = kycDtoMapper.toResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC profile created"));
	}

	@PostMapping("/{kycId}/verification-session")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Generate KYC verification session", description = "Generate a provider verification session token for the authenticated user's KYC profile")
	public ResponseEntity<ApiResponse<KycVerificationSessionResponse>> generateVerificationSession(
			Authentication authentication,
			@PathVariable String kycId) {
		var result = generateKycVerificationSessionInputPort.execute(authentication.getName(), kycId);
		var response = kycDtoMapper.toVerificationSessionResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC verification session generated"));
	}
}
