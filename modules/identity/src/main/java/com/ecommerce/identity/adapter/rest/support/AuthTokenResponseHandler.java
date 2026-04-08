package com.ecommerce.identity.adapter.rest.support;

import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.application.port.out.auth.AuthClientPolicy;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuthTokenResponseHandler {

    private final AuthClientPolicy authClientPolicy;

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

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        buildRefreshCookie(response.refreshToken(), response.expiresAt()).toString())
                .body(ApiResponse.success(webResponse, message));
    }

    public ResponseEntity<ApiResponse<Void>> logoutSuccess(String message) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(message));
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
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
