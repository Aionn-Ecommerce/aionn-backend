package com.aionn.identity.adapter.rest.support;

import com.aionn.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.aionn.identity.adapter.rest.dto.auth.LogoutAllResponse;
import com.aionn.identity.application.port.out.auth.AuthClientPolicy;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Builds the {@code AuthTokenResponse} envelope and the matching
 * {@code refresh_token} cookie. Mobile clients (identified by the configured
 * client-type header) keep the refresh token in the response body; web clients
 * receive an httpOnly cookie and a body with the refresh token blanked out.
 */
@Component
@RequiredArgsConstructor
public class AuthTokenResponseHandler {

    private final AuthClientPolicy authClientPolicy;

    @Value("${identity.auth.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${identity.auth.cookie.same-site:Strict}")
    private String cookieSameSite;

    public ResponseEntity<ApiResponse<AuthTokenResponse>> success(AuthTokenResponse response,
            HttpServletRequest request,
            String message) {
        if (isMobileClient(request)) {
            return ResponseEntity.ok(ApiResponse.success(response, message));
        }

        AuthTokenResponse webResponse = new AuthTokenResponse(
                response.userId(),
                response.sessionId(),
                null,
                response.accessToken(),
                response.expiresAt());

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (response.refreshToken() != null) {
            builder.header(HttpHeaders.SET_COOKIE,
                    buildRefreshCookie(response.refreshToken(), response.expiresAt()).toString());
        }
        return builder.body(ApiResponse.success(webResponse, message));
    }

    public ResponseEntity<ApiResponse<Void>> logoutSuccess(String message) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(message));
    }

    public ResponseEntity<ApiResponse<LogoutAllResponse>> logoutAllSuccess(LogoutAllResponse response) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(response, "All sessions revoked"));
    }

    private boolean isMobileClient(HttpServletRequest request) {
        String clientType = request == null ? null : request.getHeader(authClientPolicy.getClientTypeHeader());
        if (clientType == null || clientType.isBlank()) {
            return false;
        }
        return authClientPolicy.getMobileClientValue().equalsIgnoreCase(clientType.trim());
    }

    private ResponseCookie buildRefreshCookie(String refreshToken, LocalDateTime expiresAt) {
        long maxAgeSeconds = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        if (maxAgeSeconds < 0) {
            maxAgeSeconds = 0;
        }

        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/v1/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }
}

