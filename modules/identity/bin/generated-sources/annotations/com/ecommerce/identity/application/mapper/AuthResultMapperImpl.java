package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.auth.result.LoginResult;
import com.ecommerce.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;
import com.ecommerce.identity.domain.model.AuthSession;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:10+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AuthResultMapperImpl implements AuthResultMapper {

    @Override
    public LoginResult toLoginResult(AuthSession session, String accessToken) {
        if ( session == null && accessToken == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        LocalDateTime expiresAt = null;
        if ( session != null ) {
            userId = session.getUserId();
            sessionId = session.getSessionId();
            expiresAt = session.getExpiresAt();
        }
        String accessToken1 = null;
        accessToken1 = accessToken;

        LoginResult loginResult = new LoginResult( userId, sessionId, accessToken1, expiresAt );

        return loginResult;
    }

    @Override
    public SocialLoginResult toSocialLoginResult(AuthSession session, String accessToken, boolean newUser) {
        if ( session == null && accessToken == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        LocalDateTime expiresAt = null;
        if ( session != null ) {
            userId = session.getUserId();
            sessionId = session.getSessionId();
            expiresAt = session.getExpiresAt();
        }
        String accessToken1 = null;
        accessToken1 = accessToken;
        boolean newUser1 = false;
        newUser1 = newUser;

        SocialLoginResult socialLoginResult = new SocialLoginResult( userId, sessionId, accessToken1, expiresAt, newUser1 );

        return socialLoginResult;
    }

    @Override
    public RefreshAccessTokenResult toRefreshResult(AuthSession session, String accessToken) {
        if ( session == null && accessToken == null ) {
            return null;
        }

        String userId = null;
        String sessionId = null;
        LocalDateTime expiresAt = null;
        if ( session != null ) {
            userId = session.getUserId();
            sessionId = session.getSessionId();
            expiresAt = session.getExpiresAt();
        }
        String accessToken1 = null;
        accessToken1 = accessToken;

        RefreshAccessTokenResult refreshAccessTokenResult = new RefreshAccessTokenResult( userId, sessionId, accessToken1, expiresAt );

        return refreshAccessTokenResult;
    }
}
