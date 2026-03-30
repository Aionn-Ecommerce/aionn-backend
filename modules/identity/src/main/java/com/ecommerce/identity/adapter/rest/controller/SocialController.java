package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.LinkSocialRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialAuthRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialLinkResponse;
import com.ecommerce.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.ecommerce.sharedkernel.adapter.web.support.ClientIp;
import com.ecommerce.identity.adapter.rest.support.ClientUserAgent;
import com.ecommerce.identity.application.port.in.auth.LinkSocialInputPort;
import com.ecommerce.identity.application.port.in.auth.SocialAuthInputPort;
import com.ecommerce.identity.application.port.in.auth.UnlinkSocialInputPort;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Identity - Social Auth", description = "Identity module: social login and social account linking endpoints")
public class SocialController {

	private final SocialAuthInputPort socialAuthInputPort;
	private final LinkSocialInputPort linkSocialInputPort;
	private final UnlinkSocialInputPort unlinkSocialInputPort;
	private final AuthDtoMapper authDtoMapper;

	@PostMapping("/social")
	@Operation(summary = "Social login", description = "Authenticate via social provider token and create auth session")
	public ResponseEntity<ApiResponse<AuthTokenResponse>> socialLogin(
			@Valid @RequestBody SocialAuthRequest request,
			@ClientIp String clientIp,
			@ClientUserAgent String userAgent) {
		var result = socialAuthInputPort.execute(authDtoMapper.toSocialLoginCommand(request, clientIp, userAgent));
		AuthTokenResponse response = authDtoMapper.toAuthTokenResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Social login successful!"));
	}

	@PostMapping("/social-links")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Link social account", description = "Link a social provider account to the authenticated user")
	public ResponseEntity<ApiResponse<SocialLinkResponse>> linkSocial(
			Authentication authentication,
			@Valid @RequestBody LinkSocialRequest request) {
		String userId = authentication.getName();
		var result = linkSocialInputPort.execute(authDtoMapper.toLinkSocialCommand(userId, request));
		SocialLinkResponse response = authDtoMapper.toSocialLinkResponse(result);
		return ApiResponse.createdResponse("Social account linked successfully!", response);
	}

	@DeleteMapping("/social-links/{provider}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Unlink social account", description = "Unlink one social provider account from the authenticated user")
	public ResponseEntity<Void> unlinkSocial(
			Authentication authentication,
			@PathVariable String provider) {
		String userId = authentication.getName();
		unlinkSocialInputPort.execute(authDtoMapper.toUnlinkSocialCommand(userId, provider));
		return ResponseEntity.noContent().build();
	}
}
