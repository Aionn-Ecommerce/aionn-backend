package com.aionn.identity.adapter.rest.controller;

import com.aionn.identity.adapter.rest.dto.kyc.CreateKycRequest;
import com.aionn.identity.adapter.rest.dto.kyc.KycResponse;
import com.aionn.identity.adapter.rest.dto.kyc.RejectKycRequest;
import com.aionn.identity.adapter.rest.dto.kyc.ReviewKycRequest;
import com.aionn.identity.adapter.rest.dto.kyc.UploadKycDocumentRequest;
import com.aionn.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.aionn.identity.application.port.in.kyc.*;
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
@Tag(name = "Identity - KYC", description = "Identity module: KYC submission and review endpoints")
public class KycController {

	private final ListMyKycQueryPort listMyKycQueryPort;
	private final GetKycQueryPort getKycQueryPort;
	private final CreateKycInputPort createKycInputPort;
	private final UploadKycDocumentInputPort uploadKycDocumentInputPort;
	private final SubmitKycInputPort submitKycInputPort;
	private final CancelKycInputPort cancelKycInputPort;
	private final ReviewKycInputPort reviewKycInputPort;
	private final ApproveKycInputPort approveKycInputPort;
	private final RejectKycInputPort rejectKycInputPort;
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

	@PostMapping("/{kycId}/documents")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Upload KYC document metadata", description = "Attach uploaded document metadata to a KYC profile")
	public ResponseEntity<ApiResponse<KycResponse>> uploadDocument(
			Authentication authentication,
			@PathVariable String kycId,
			@Valid @RequestBody UploadKycDocumentRequest request) {
		var result = uploadKycDocumentInputPort
				.execute(kycDtoMapper.toUploadDocumentCommand(authentication.getName(), kycId, request));
		var response = kycDtoMapper.toResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC document uploaded"));
	}

	@PostMapping("/{kycId}/submit")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Submit KYC", description = "Submit a KYC profile for review")
	public ResponseEntity<ApiResponse<KycResponse>> submit(
			Authentication authentication,
			@PathVariable String kycId) {
		var result = submitKycInputPort.execute(kycDtoMapper.toSubmitKycCommand(authentication.getName(), kycId));
		var response = kycDtoMapper.toResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC submitted"));
	}

	@DeleteMapping("/{kycId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Cancel KYC", description = "Cancel a pending KYC profile by KYC ID")
	public ResponseEntity<Void> cancel(
			Authentication authentication,
			@PathVariable String kycId) {
		cancelKycInputPort.execute(kycDtoMapper.toCancelKycCommand(authentication.getName(), kycId));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/admin/{kycId}/review")
	@PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
	@Operation(summary = "Review KYC", description = "Set a KYC profile to in-review state as admin")
	public ResponseEntity<ApiResponse<KycResponse>> review(
			Authentication authentication,
			@PathVariable String kycId,
			@Valid @RequestBody ReviewKycRequest request) {
		var result = reviewKycInputPort.execute(kycDtoMapper.toReviewCommand(authentication.getName(), kycId, request));
		var response = kycDtoMapper.toResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC in review"));
	}

	@PostMapping("/admin/{kycId}/approve")
	@PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
	@Operation(summary = "Approve KYC", description = "Approve a KYC profile as admin")
	public ResponseEntity<ApiResponse<KycResponse>> approve(
			Authentication authentication,
			@PathVariable String kycId) {
		var result = approveKycInputPort.execute(kycDtoMapper.toApproveKycCommand(authentication.getName(), kycId));
		var response = kycDtoMapper.toResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC approved"));
	}

	@PostMapping("/admin/{kycId}/reject")
	@PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
	@Operation(summary = "Reject KYC", description = "Reject a KYC profile with reason as admin")
	public ResponseEntity<ApiResponse<KycResponse>> reject(
			Authentication authentication,
			@PathVariable String kycId,
			@Valid @RequestBody RejectKycRequest request) {
		var result = rejectKycInputPort.execute(kycDtoMapper.toRejectCommand(authentication.getName(), kycId, request));
		var response = kycDtoMapper.toResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "KYC rejected"));
	}
}



