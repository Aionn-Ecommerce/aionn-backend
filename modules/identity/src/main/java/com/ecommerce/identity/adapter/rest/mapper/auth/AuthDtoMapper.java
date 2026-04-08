package com.ecommerce.identity.adapter.rest.mapper.auth;

import com.ecommerce.identity.adapter.rest.dto.auth.AuthSessionResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.LinkSocialRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.LoginRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.LogoutAllResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.RefreshTokenRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialAuthRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialAuthResponse;
import com.ecommerce.identity.adapter.rest.dto.auth.SocialLinkResponse;
import com.ecommerce.identity.application.dto.auth.command.LinkSocialCommand;
import com.ecommerce.identity.application.dto.auth.command.LoginCommand;
import com.ecommerce.identity.application.dto.auth.command.LogoutAllCommand;
import com.ecommerce.identity.application.dto.auth.command.LogoutCommand;
import com.ecommerce.identity.application.dto.auth.command.RefreshTokenCommand;
import com.ecommerce.identity.application.dto.auth.command.RevokeSessionCommand;
import com.ecommerce.identity.application.dto.auth.command.SocialLoginCommand;
import com.ecommerce.identity.application.dto.auth.command.UnlinkSocialCommand;
import com.ecommerce.identity.application.dto.auth.query.GetAuthSessionsQuery;
import com.ecommerce.identity.application.dto.auth.result.LoginResult;
import com.ecommerce.identity.application.dto.auth.result.LogoutAllResult;
import com.ecommerce.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;
import com.ecommerce.identity.application.dto.auth.view.AuthSessionView;
import com.ecommerce.identity.application.dto.auth.view.SocialLinkView;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.SocialLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

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

    default GetAuthSessionsQuery toGetSessionsQuery(String userId) {
        return new GetAuthSessionsQuery(userId);
    }

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "provider", source = "request.provider")
    @Mapping(target = "providerToken", source = "request.providerToken")
    LinkSocialCommand toLinkSocialCommand(String userId, LinkSocialRequest request);

    default UnlinkSocialCommand toUnlinkSocialCommand(String userId, String provider) {
        return new UnlinkSocialCommand(userId, provider);
    }

    default RevokeSessionCommand toRevokeSessionCommand(String userId, String sessionId) {
        return new RevokeSessionCommand(userId, sessionId);
    }

    default LogoutCommand toLogoutCommand(String userId, String userAgent) {
        return new LogoutCommand(userId, userAgent);
    }

    default LogoutAllCommand toLogoutAllCommand(String userId) {
        return new LogoutAllCommand(userId);
    }

    default AuthSessionView toAuthSessionView(AuthSession session) {
        return new AuthSessionView(
                session.getSessionId(),
                session.getUserId(),
                session.getStatus().name(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getCreatedAt(),
                session.getLastActiveAt(),
                session.getExpiresAt());
    }

    List<AuthSessionView> toAuthSessionViews(List<AuthSession> sessions);

    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "providerUserId", source = "providerUserId")
    @Mapping(target = "linkedAt", source = "createdAt")
    SocialLinkView toSocialLinkView(SocialLink socialLink);

    AuthTokenResponse toAuthTokenResponse(LoginResult result);

    AuthTokenResponse toAuthTokenResponse(RefreshAccessTokenResult result);

    AuthTokenResponse toAuthTokenResponse(SocialLoginResult result);

    SocialAuthResponse toSocialAuthResponse(LoginResult result);

    SocialAuthResponse toSocialLoginResponse(SocialLoginResult result);

    SocialLinkResponse toSocialLinkResponse(SocialLinkView view);

    AuthSessionResponse toAuthSessionResponse(AuthSessionView view);

    List<AuthSessionResponse> toAuthSessionResponses(List<AuthSessionView> views);

    LogoutAllResponse toLogoutAllResponse(LogoutAllResult result);
}
