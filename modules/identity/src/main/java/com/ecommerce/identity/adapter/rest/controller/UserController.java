package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.user.*;
import com.ecommerce.identity.adapter.rest.mapper.user.UserDtoMapper;
import com.ecommerce.identity.application.port.in.user.*;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Identity - User Self-Service", description = "Identity module: current user profile and account self-service endpoints")
public class UserController {

	private final GetMyProfileInputPort getMyProfileInputPort;
	private final VerifyEmailInputPort verifyEmailInputPort;
	private final UpdateDisplayNameInputPort updateDisplayNameInputPort;
	private final UpdateAvatarInputPort updateAvatarInputPort;
	private final ChangeEmailInputPort changeEmailInputPort;
	private final ChangePhoneInputPort changePhoneInputPort;
	private final RequestAccountDeletionInputPort requestAccountDeletionInputPort;
	private final CancelAccountDeletionInputPort cancelAccountDeletionInputPort;
	private final RequestDataExportInputPort requestDataExportInputPort;
	private final UserDtoMapper userDtoMapper;

	@GetMapping
	@Operation(summary = "Get my profile", description = "Get profile information for the authenticated user")
	public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
		var result = getMyProfileInputPort.execute(userDtoMapper.toGetMyProfileQuery(authentication.getName()));
		UserProfileResponse response = userDtoMapper.toProfileResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Fetched user profile"));
	}

	@PostMapping("/verify-email")
	@Operation(summary = "Verify email", description = "Verify user email address using verification token or code")
	public ResponseEntity<ApiResponse<UserActionResponse>> verifyEmail(
			Authentication authentication,
			@Valid @RequestBody VerifyEmailRequest request) {
		var result = verifyEmailInputPort
				.execute(userDtoMapper.toVerifyEmailCommand(authentication.getName(), request));
		UserActionResponse response = userDtoMapper.toActionResponse(result.action());
		return ResponseEntity.ok(ApiResponse.success(response, result.message()));
	}

	@PatchMapping("/display-name")
	@Operation(summary = "Update display name", description = "Update display name for the authenticated user")
	public ResponseEntity<ApiResponse<UserProfileResponse>> updateDisplayName(
			Authentication authentication,
			@Valid @RequestBody ChangeDisplayNameRequest request) {
		var result = updateDisplayNameInputPort
				.execute(userDtoMapper.toUpdateDisplayNameCommand(authentication.getName(), request));
		UserProfileResponse response = userDtoMapper.toProfileResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Display name updated"));
	}

	@PatchMapping("/avatar")
	@Operation(summary = "Update avatar", description = "Update avatar URL or avatar reference for the authenticated user")
	public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatar(
			Authentication authentication,
			@Valid @RequestBody ChangeAvatarRequest request) {
		var result = updateAvatarInputPort
				.execute(userDtoMapper.toUpdateAvatarCommand(authentication.getName(), request));
		UserProfileResponse response = userDtoMapper.toProfileResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Avatar updated"));
	}

	@PatchMapping("/email")
	@Operation(summary = "Change email", description = "Request or complete email change flow for the authenticated user")
	public ResponseEntity<ApiResponse<?>> changeEmail(
			Authentication authentication,
			@Valid @RequestBody ChangeEmailRequest request) {
		var result = changeEmailInputPort
				.execute(userDtoMapper.toChangeEmailCommand(authentication.getName(), request));
		var response = userDtoMapper.toOutcomeResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, result.message()));
	}

	@PatchMapping("/phone")
	@Operation(summary = "Change phone", description = "Request or complete phone change flow for the authenticated user")
	public ResponseEntity<ApiResponse<?>> changePhone(
			Authentication authentication,
			@Valid @RequestBody ChangePhoneRequest request) {
		var result = changePhoneInputPort
				.execute(userDtoMapper.toChangePhoneCommand(authentication.getName(), request));
		var response = userDtoMapper.toOutcomeResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, result.message()));
	}

	@PostMapping("/deletion-requests")
	@Operation(summary = "Request account deletion", description = "Create account deletion request for the authenticated user")
	public ResponseEntity<ApiResponse<DeletionRequestResponse>> requestAccountDeletion(Authentication authentication) {
		var result = requestAccountDeletionInputPort
				.execute(userDtoMapper.toRequestAccountDeletionCommand(authentication.getName()));
		DeletionRequestResponse response = userDtoMapper.toDeletionRequestResponse(result);
		return ApiResponse.createdResponse("Account deletion requested", response);
	}

	@DeleteMapping("/deletion-requests")
	@Operation(summary = "Cancel account deletion", description = "Cancel existing account deletion request for the authenticated user")
	public ResponseEntity<Void> cancelAccountDeletion(Authentication authentication) {
		cancelAccountDeletionInputPort.execute(userDtoMapper.toCancelAccountDeletionCommand(authentication.getName()));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/data-exports")
	@Operation(summary = "Request data export", description = "Create personal data export request for the authenticated user")
	public ResponseEntity<ApiResponse<DataExportRequestResponse>> requestDataExport(Authentication authentication) {
		var result = requestDataExportInputPort
				.execute(userDtoMapper.toRequestDataExportCommand(authentication.getName()));
		DataExportRequestResponse response = userDtoMapper.toDataExportResponse(result);
		return ApiResponse.createdResponse("Data export requested", response);
	}
}


