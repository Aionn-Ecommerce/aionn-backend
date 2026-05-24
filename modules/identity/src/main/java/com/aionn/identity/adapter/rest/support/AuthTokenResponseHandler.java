package com.aionn.identity.adapter.rest.support;

import com.aionn.identity.adapter.rest.dto.auth.response.AuthTokenResponse;
import com.aionn.identity.adapter.rest.dto.auth.response.LogoutAllResponse;
import com.aionn.identity.application.port.out.auth.AuthClientPolicy;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
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

    @Value("${identity.auth.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${identity.auth.cookie.same-site:Strict}")
    private String cookieSameSite;

    public ResponseEntity<ApiResponse<AuthTokenResponse>> success(
            AuthTokenResponse response,
            String clientType,
            String message) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        applyNoStore(builder);

        if (isMobileClient(clientType)) {
            return builder.body(ApiResponse.success(response, message));
        }

        AuthTokenResponse webResponse = new AuthTokenResponse(
                response.userId(),
                response.sessionId(),
                null,
                response.accessToken(),
                response.expiresAt(),
                response.sessionExpiresAt());

        if (response.refreshToken() != null) {
            builder.header(HttpHeaders.SET_COOKIE,
                    buildRefreshCookie(response.refreshToken(), response.sessionExpiresAt()).toString());
        }
        return builder.body(ApiResponse.success(webResponse, message));
    }

    public ResponseEntity<ApiResponse<Void>> logoutSuccess(String message) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        applyNoStore(builder);
        return builder
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(message));
    }

    public ResponseEntity<ApiResponse<LogoutAllResponse>> logoutAllSuccess(LogoutAllResponse response) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        applyNoStore(builder);
        return builder
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success(response, "All sessions revoked"));
    }

    public void applyNoStore(ResponseEntity.BodyBuilder builder) {
        builder.cacheControl(CacheControl.noStore().mustRevalidate().cachePrivate())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0");
    }

    private boolean isMobileClient(String clientType) {
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
