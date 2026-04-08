package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.auth.*;
import com.ecommerce.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.ecommerce.identity.adapter.rest.support.AuthTokenResponseHandler;
import com.ecommerce.sharedkernel.adapter.web.support.ClientIp;
import com.ecommerce.identity.adapter.rest.support.ClientUserAgent;
import com.ecommerce.identity.application.port.in.auth.*;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Identity - Auth", description = "Identity module: credential login and session endpoints")
public class AuthController {

	private final LoginInputPort loginInputPort;
	private final RefreshTokenInputPort refreshTokenInputPort;
	private final GetAuthSessionsQueryPort getAuthSessionsQueryPort;
	private final RevokeSessionInputPort revokeSessionInputPort;
	private final LogoutAllInputPort logoutAllInputPort;
	private final LogoutInputPort logoutInputPort;
	private final AuthDtoMapper authDtoMapper;
	private final AuthTokenResponseHandler authTokenResponseHandler;

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Authenticate user credentials and create a new auth session")
	public ResponseEntity<ApiResponse<AuthTokenResponse>> login(
			@Valid @RequestBody LoginRequest request,
			@ClientIp String clientIp,
			@ClientUserAgent String userAgent,
			HttpServletRequest httpRequest) {
		var result = loginInputPort.execute(authDtoMapper.toLoginCommand(request, clientIp, userAgent));
		AuthTokenResponse response = authDtoMapper.toAuthTokenResponse(result);
		return authTokenResponseHandler.success(response, httpRequest, "Login successful!");
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh token", description = "Refresh access token using refresh token from body or cookie")
	public ResponseEntity<ApiResponse<AuthTokenResponse>> refreshToken(
			@RequestBody(required = false) RefreshTokenRequest request,
			@CookieValue(name = "refresh_token", required = false) String cookieToken,
			@ClientIp String clientIp,
			@ClientUserAgent String userAgent,
			HttpServletRequest httpRequest) {
		var result = refreshTokenInputPort
				.execute(authDtoMapper.toRefreshCommand(request, cookieToken, clientIp, userAgent));
		AuthTokenResponse response = authDtoMapper.toAuthTokenResponse(result);
		return authTokenResponseHandler.success(response, httpRequest, "Token refreshed successfully!");
	}

	@GetMapping("/sessions")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List sessions", description = "Get active auth sessions for the authenticated user")
	public ResponseEntity<ApiResponse<List<AuthSessionResponse>>> getSessions(
			Authentication authentication) {
		var query = authDtoMapper.toGetSessionsQuery(authentication.getName());
		var result = getAuthSessionsQueryPort.execute(query);
		List<AuthSessionResponse> sessions = authDtoMapper.toAuthSessionResponses(result);
		return ResponseEntity.ok(ApiResponse.success(sessions, "Sessions fetched successfully!"));
	}

	@DeleteMapping("/sessions/{sessionId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Revoke session", description = "Revoke one auth session by session ID")
	public ResponseEntity<Void> revokeSession(
			Authentication authentication,
			@PathVariable String sessionId) {
		var command = authDtoMapper.toRevokeSessionCommand(authentication.getName(), sessionId);
		revokeSessionInputPort.execute(command);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/logout")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Logout", description = "Logout current user from the current session")
	public ResponseEntity<ApiResponse<Void>> logout(
			Authentication authentication,
			@ClientUserAgent String userAgent) {
		var command = authDtoMapper.toLogoutCommand(authentication.getName(), userAgent);
		logoutInputPort.execute(command);
		return authTokenResponseHandler.logoutSuccess("Logout successful");
	}

	@PostMapping("/logout-all")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Logout all sessions", description = "Revoke all active sessions for the authenticated user")
	public ResponseEntity<ApiResponse<LogoutAllResponse>> logoutAll(
			Authentication authentication) {
		var command = authDtoMapper.toLogoutAllCommand(authentication.getName());
		var result = logoutAllInputPort.execute(command);
		LogoutAllResponse response = authDtoMapper.toLogoutAllResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "All sessions revoked"));
	}

}
