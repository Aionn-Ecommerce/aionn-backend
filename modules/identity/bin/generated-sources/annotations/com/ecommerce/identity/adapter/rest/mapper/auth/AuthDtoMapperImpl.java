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
import com.ecommerce.identity.application.dto.auth.command.RefreshTokenCommand;
import com.ecommerce.identity.application.dto.auth.command.SocialLoginCommand;
import com.ecommerce.identity.application.dto.auth.result.LoginResult;
import com.ecommerce.identity.application.dto.auth.result.LogoutAllResult;
import com.ecommerce.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;
import com.ecommerce.identity.application.dto.auth.view.AuthSessionView;
import com.ecommerce.identity.application.dto.auth.view.SocialLinkView;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.SocialLink;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AuthDtoMapperImpl implements AuthDtoMapper {

    @Override
    public LoginCommand toLoginCommand(LoginRequest request, String clientIp, String userAgent) {
        if ( request == null && clientIp == null && userAgent == null ) {
            return null;
        }

        String identity = null;
        String password = null;
        if ( request != null ) {
            identity = request.identity();
            password = request.password();
        }
        String ipAddress = null;
        ipAddress = clientIp;
        String userAgent1 = null;
        userAgent1 = userAgent;

        LoginCommand loginCommand = new LoginCommand( identity, password, ipAddress, userAgent1 );

        return loginCommand;
    }

    @Override
    public SocialLoginCommand toSocialLoginCommand(SocialAuthRequest request, String clientIp, String userAgent) {
        if ( request == null && clientIp == null && userAgent == null ) {
            return null;
        }

        String provider = null;
        String providerToken = null;
        if ( request != null ) {
            provider = request.provider();
            providerToken = request.providerToken();
        }
        String ipAddress = null;
        ipAddress = clientIp;
        String userAgent1 = null;
        userAgent1 = userAgent;

        SocialLoginCommand socialLoginCommand = new SocialLoginCommand( provider, providerToken, ipAddress, userAgent1 );

        return socialLoginCommand;
    }

    @Override
    public RefreshTokenCommand toRefreshCommand(RefreshTokenRequest request, String cookieToken, String clientIp, String userAgent) {
        if ( request == null && cookieToken == null && clientIp == null && userAgent == null ) {
            return null;
        }

        String requestRefreshToken = null;
        if ( request != null ) {
            requestRefreshToken = request.refreshToken();
        }
        String cookieRefreshToken = null;
        cookieRefreshToken = cookieToken;
        String clientIp1 = null;
        clientIp1 = clientIp;
        String userAgent1 = null;
        userAgent1 = userAgent;

        RefreshTokenCommand refreshTokenCommand = new RefreshTokenCommand( requestRefreshToken, cookieRefreshToken, clientIp1, userAgent1 );

        return refreshTokenCommand;
    }

    @Override
    public LinkSocialCommand toLinkSocialCommand(String userId, LinkSocialRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String provider = null;
        String providerToken = null;
        if ( request != null ) {
            provider = request.provider();
            providerToken = request.providerToken();
        }
        String userId1 = null;
        userId1 = userId;

        LinkSocialCommand linkSocialCommand = new LinkSocialCommand( userId1, provider, providerToken );

        return linkSocialCommand;
    }

    @Override
    public List<AuthSessionView> toAuthSessionViews(List<AuthSession> sessions) {
        if ( sessions == null ) {
            return null;
        }

        List<AuthSessionView> list = new ArrayList<AuthSessionView>( sessions.size() );
        for ( AuthSession authSession : sessions ) {
            list.add( toAuthSessionView( authSession ) );
        }

        return list;
    }

    @Override
    public SocialLinkView toSocialLinkView(SocialLink socialLink) {
        if ( socialLink == null ) {
            return null;
        }

        String provider = null;
        String providerUserId = null;
        LocalDateTime linkedAt = null;

        if ( socialLink.provider() != null ) {
            provider = socialLink.provider().name();
        }
        providerUserId = socialLink.providerUserId();
        linkedAt = socialLink.createdAt();

        SocialLinkView socialLinkView = new SocialLinkView( provider, providerUserId, linkedAt );

        return socialLinkView;
    }

    @Override
    public AuthTokenResponse toAuthTokenResponse(LoginResult result) {
        if ( result == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        String accessToken = null;
        LocalDateTime expiresAt = null;

        userId = result.userId();
        sessionId = result.sessionId();
        accessToken = result.accessToken();
        expiresAt = result.expiresAt();

        String refreshToken = null;

        AuthTokenResponse authTokenResponse = new AuthTokenResponse( userId, sessionId, refreshToken, accessToken, expiresAt );

        return authTokenResponse;
    }

    @Override
    public AuthTokenResponse toAuthTokenResponse(RefreshAccessTokenResult result) {
        if ( result == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        String accessToken = null;
        LocalDateTime expiresAt = null;

        userId = result.userId();
        sessionId = result.sessionId();
        accessToken = result.accessToken();
        expiresAt = result.expiresAt();

        String refreshToken = null;

        AuthTokenResponse authTokenResponse = new AuthTokenResponse( userId, sessionId, refreshToken, accessToken, expiresAt );

        return authTokenResponse;
    }

    @Override
    public AuthTokenResponse toAuthTokenResponse(SocialLoginResult result) {
        if ( result == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        String accessToken = null;
        LocalDateTime expiresAt = null;

        userId = result.userId();
        sessionId = result.sessionId();
        accessToken = result.accessToken();
        expiresAt = result.expiresAt();

        String refreshToken = null;

        AuthTokenResponse authTokenResponse = new AuthTokenResponse( userId, sessionId, refreshToken, accessToken, expiresAt );

        return authTokenResponse;
    }

    @Override
    public SocialAuthResponse toSocialAuthResponse(LoginResult result) {
        if ( result == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        String accessToken = null;
        LocalDateTime expiresAt = null;

        userId = result.userId();
        sessionId = result.sessionId();
        accessToken = result.accessToken();
        expiresAt = result.expiresAt();

        SocialAuthResponse socialAuthResponse = new SocialAuthResponse( userId, sessionId, accessToken, expiresAt );

        return socialAuthResponse;
    }

    @Override
    public SocialAuthResponse toSocialLoginResponse(SocialLoginResult result) {
        if ( result == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        String accessToken = null;
        LocalDateTime expiresAt = null;

        userId = result.userId();
        sessionId = result.sessionId();
        accessToken = result.accessToken();
        expiresAt = result.expiresAt();

        SocialAuthResponse socialAuthResponse = new SocialAuthResponse( userId, sessionId, accessToken, expiresAt );

        return socialAuthResponse;
    }

    @Override
    public SocialLinkResponse toSocialLinkResponse(SocialLinkView view) {
        if ( view == null ) {
            return null;
        }

        String provider = null;
        String providerUserId = null;
        LocalDateTime linkedAt = null;

        provider = view.provider();
        providerUserId = view.providerUserId();
        linkedAt = view.linkedAt();

        SocialLinkResponse socialLinkResponse = new SocialLinkResponse( provider, providerUserId, linkedAt );

        return socialLinkResponse;
    }

    @Override
    public AuthSessionResponse toAuthSessionResponse(AuthSessionView view) {
        if ( view == null ) {
            return null;
        }

        String sessionId = null;
        String status = null;
        String ipAddress = null;
        String userAgent = null;
        LocalDateTime createdAt = null;
        LocalDateTime lastActiveAt = null;
        LocalDateTime expiresAt = null;

        sessionId = view.sessionId();
        status = view.status();
        ipAddress = view.ipAddress();
        userAgent = view.userAgent();
        createdAt = view.createdAt();
        lastActiveAt = view.lastActiveAt();
        expiresAt = view.expiresAt();

        AuthSessionResponse authSessionResponse = new AuthSessionResponse( sessionId, status, ipAddress, userAgent, createdAt, lastActiveAt, expiresAt );

        return authSessionResponse;
    }

    @Override
    public List<AuthSessionResponse> toAuthSessionResponses(List<AuthSessionView> views) {
        if ( views == null ) {
            return null;
        }

        List<AuthSessionResponse> list = new ArrayList<AuthSessionResponse>( views.size() );
        for ( AuthSessionView authSessionView : views ) {
            list.add( toAuthSessionResponse( authSessionView ) );
        }

        return list;
    }

    @Override
    public LogoutAllResponse toLogoutAllResponse(LogoutAllResult result) {
        if ( result == null ) {
            return null;
        }

        int revokedSessions = 0;

        revokedSessions = result.revokedSessions();

        LogoutAllResponse logoutAllResponse = new LogoutAllResponse( revokedSessions );

        return logoutAllResponse;
    }
}
