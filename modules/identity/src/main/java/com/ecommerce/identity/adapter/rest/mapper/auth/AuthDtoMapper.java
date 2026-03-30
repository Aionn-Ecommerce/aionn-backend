package com.ecommerce.identity.adapter.rest.mapper.auth;

import com.ecommerce.identity.adapter.rest.dto.auth.AuthSessionResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.LinkSocialRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.LoginRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.LoginResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.LogoutAllResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.RefreshTokenRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialAuthRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialAuthResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialLinkResponse;
import com.ecommerce.identity.application.dto.auth.AuthSessionView;
import com.ecommerce.identity.application.dto.auth.GetAuthSessionsQuery;
import com.ecommerce.identity.application.dto.auth.LinkSocialCommand;
import com.ecommerce.identity.application.dto.auth.LoginCommand;
import com.ecommerce.identity.application.dto.auth.LoginResult;
import com.ecommerce.identity.application.dto.auth.LogoutAllCommand;
import com.ecommerce.identity.application.dto.auth.LogoutAllResult;
import com.ecommerce.identity.application.dto.auth.LogoutCommand;
import com.ecommerce.identity.application.dto.auth.RefreshTokenCommand;
import com.ecommerce.identity.application.dto.auth.RevokeSessionCommand;
import com.ecommerce.identity.application.dto.auth.SocialLoginCommand;
import com.ecommerce.identity.application.dto.auth.SocialLoginResult;
import com.ecommerce.identity.application.dto.auth.SocialLinkView;
import com.ecommerce.identity.application.dto.auth.UnlinkSocialCommand;
import com.ecommerce.identity.infrastructure.persistence.entity.AuthSessionEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.SocialAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

    // Request -> Command
    @Mapping(target = "identity", source = "request.identity")
    @Mapping(target = "password", source = "request.password")
    @Mapping(target = "ipAddress", source = "clientIp")
    @Mapping(target = "userAgent", source = "userAgent")
    LoginCommand toLoginCommand(LoginRequest request, String clientIp, String userAgent);

    @Mapping(target = "provider", source = "request.provider")
    @Mapping(target = "providerToken", source = "request.providerToken")
    @Mapping(target = "ipAddress", source = "clientIp")
    @Mapping(target = "userAgent", source = "userAgent")
    SocialLoginCommand toSocialLoginCommand(SocialAuthRequest request, String clientIp, String userAgent);

    @Mapping(target = "requestRefreshToken", source = "request.refreshToken")
    @Mapping(target = "cookieRefreshToken", source = "cookieToken")
    @Mapping(target = "clientIp", source = "clientIp")
    @Mapping(target = "userAgent", source = "userAgent")
    RefreshTokenCommand toRefreshCommand(RefreshTokenRequest request, String cookieToken, String clientIp,
            String userAgent);

    GetAuthSessionsQuery toGetSessionsQuery(String userId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "provider", source = "request.provider")
    @Mapping(target = "providerToken", source = "request.providerToken")
    LinkSocialCommand toLinkSocialCommand(String userId, LinkSocialRequest request);

    UnlinkSocialCommand toUnlinkSocialCommand(String userId, String provider);

    RevokeSessionCommand toRevokeSessionCommand(String userId, String sessionId);

    LogoutCommand toLogoutCommand(String userId, String userAgent);

    LogoutAllCommand toLogoutAllCommand(String userId);

    // Entity -> Result
    @Mapping(target = "userId", source = "session.user.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    LoginResult toLoginResult(AuthSessionEntity session, String accessToken);

    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "providerUserId", source = "providerUserId")
    @Mapping(target = "linkedAt", source = "createdAt")
    SocialLinkView toSocialLinkView(SocialAccountEntity entity);

    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "userAgent", source = "userAgent")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastActiveAt", source = "lastActiveAt")
    @Mapping(target = "expiresAt", source = "expiresAt")
    AuthSessionView toAuthSessionView(AuthSessionEntity entity);

    List<AuthSessionView> toAuthSessionViews(List<AuthSessionEntity> entities);

    // Result -> Response
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "expiresAt", source = "expiresAt")
    AuthTokenResponse toAuthTokenResponse(LoginResult result);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "expiresAt", source = "expiresAt")
    SocialAuthResponse toSocialAuthResponse(LoginResult result);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "expiresAt", source = "expiresAt")
    SocialAuthResponse toSocialLoginResponse(SocialLoginResult result);

    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "providerUserId", source = "providerUserId")
    @Mapping(target = "linkedAt", source = "linkedAt")
    SocialLinkResponse toSocialLinkResponse(SocialLinkView view);

    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "userAgent", source = "userAgent")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastActiveAt", source = "lastActiveAt")
    @Mapping(target = "expiresAt", source = "expiresAt")
    AuthSessionResponse toAuthSessionResponse(AuthSessionView view);

    List<AuthSessionResponse> toAuthSessionResponses(List<AuthSessionView> views);

    LogoutAllResponse toLogoutAllResponse(LogoutAllResult result);

    // Helper
    default LogoutAllResult toLogoutAllResult(int revokedCount) {
        return new LogoutAllResult(revokedCount);
    }
}
