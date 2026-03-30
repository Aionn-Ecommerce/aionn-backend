package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.security.*;
import com.ecommerce.identity.adapter.rest.mapper.security.SecurityDtoMapper;
import com.ecommerce.sharedkernel.adapter.web.support.ClientIp;
import com.ecommerce.identity.application.port.in.security.*;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
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
@RequestMapping("/api/v1/security")
@RequiredArgsConstructor
@Tag(name = "Identity - Security", description = "Identity module: password, MFA, audit log, and account unlock endpoints")
public class SecurityController {

	private final ChangePasswordInputPort changePasswordInputPort;
	private final RequestPasswordResetInputPort requestPasswordResetInputPort;
	private final CompletePasswordResetInputPort completePasswordResetInputPort;
	private final EnableMfaInputPort enableMfaInputPort;
	private final DisableMfaInputPort disableMfaInputPort;
	private final RegenerateBackupCodesInputPort regenerateBackupCodesInputPort;
	private final GetSecurityAuditLogsQueryPort getSecurityAuditLogsQueryPort;
	private final UnlockAccountInputPort unlockAccountInputPort;
	private final SecurityDtoMapper securityDtoMapper;

	@PutMapping("/password")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Change password", description = "Change password for the authenticated user")
	public ResponseEntity<ApiResponse<Void>> changePassword(
			Authentication authentication,
			@ClientIp String clientIp,
			@Valid @RequestBody ChangePasswordRequest request) {
		changePasswordInputPort
				.execute(securityDtoMapper.toChangePasswordCommand(authentication.getName(), clientIp, request));
		return ResponseEntity.ok(ApiResponse.success("Password changed"));
	}

	@PostMapping("/password-reset-requests")
	@Operation(summary = "Request password reset", description = "Create a password reset request for an account")
	public ResponseEntity<ApiResponse<PasswordResetResponse>> requestPasswordReset(
			@ClientIp String clientIp,
			@Valid @RequestBody PasswordResetRequestCommand request) {
		var result = requestPasswordResetInputPort.execute(securityDtoMapper.toPasswordResetCommand(clientIp, request));
		var response = securityDtoMapper.toPasswordResetResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Password reset requested"));
	}

	@PostMapping("/password-reset")
	@Operation(summary = "Complete password reset", description = "Complete password reset using reset token and new password")
	public ResponseEntity<ApiResponse<Void>> completePasswordReset(
			@ClientIp String clientIp,
			@Valid @RequestBody CompletePasswordResetRequest request) {
		completePasswordResetInputPort.execute(securityDtoMapper.toCompletePasswordResetCommand(clientIp, request));
		return ResponseEntity.ok(ApiResponse.success("Password reset completed"));
	}

	@PostMapping("/mfa/enable")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Enable MFA", description = "Enable multi-factor authentication for the authenticated user")
	public ResponseEntity<ApiResponse<MfaResponse>> enableMfa(
			Authentication authentication,
			@ClientIp String clientIp,
			@Valid @RequestBody MfaToggleRequest request) {
		var result = enableMfaInputPort
				.execute(securityDtoMapper.toEnableMfaCommand(authentication.getName(), clientIp, request));
		var response = securityDtoMapper.toMfaResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "MFA enabled"));
	}

	@PostMapping("/mfa/disable")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Disable MFA", description = "Disable multi-factor authentication for the authenticated user")
	public ResponseEntity<ApiResponse<MfaResponse>> disableMfa(
			Authentication authentication,
			@ClientIp String clientIp,
			@Valid @RequestBody MfaToggleRequest request) {
		var result = disableMfaInputPort
				.execute(securityDtoMapper.toDisableMfaCommand(authentication.getName(), clientIp, request));
		var response = securityDtoMapper.toMfaResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "MFA disabled"));
	}

	@PostMapping("/mfa/backup-codes")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Regenerate backup codes", description = "Regenerate MFA backup codes for the authenticated user")
	public ResponseEntity<ApiResponse<BackupCodesResponse>> regenerateBackupCodes(
			Authentication authentication,
			@ClientIp String clientIp) {
		var result = regenerateBackupCodesInputPort
				.execute(securityDtoMapper.toRegenerateBackupCodesCommand(authentication.getName(), clientIp));
		var response = securityDtoMapper.toBackupCodesResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Backup codes regenerated"));
	}

	@GetMapping("/audit-logs")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get security audit logs", description = "Get security-related audit logs for the authenticated user")
	public ResponseEntity<ApiResponse<List<SecurityAuditLogResponse>>> getAuditLogs(Authentication authentication) {
		var result = getSecurityAuditLogsQueryPort.execute(authentication.getName());
		var response = securityDtoMapper.toAuditLogResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Security audit logs"));
	}

	@PostMapping("/admin/unlock-account")
	@PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
	@Operation(summary = "Unlock account", description = "Unlock a locked user account by admin")
	public ResponseEntity<ApiResponse<Void>> unlockAccount(@Valid @RequestBody UnlockAccountRequest request) {
		unlockAccountInputPort.execute(securityDtoMapper.toUnlockAccountCommand(request.userId()));
		return ResponseEntity.ok(ApiResponse.success("Account unlocked"));
	}
}
