package com.aionn.identity.application.mapper;

import com.aionn.identity.application.dto.auth.result.LoginResult;
import com.aionn.identity.application.dto.auth.result.LogoutAllResult;
import com.aionn.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.aionn.identity.application.dto.auth.result.SocialLoginResult;
import com.aionn.identity.domain.model.AuthSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface AuthResultMapper {

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "sessionExpiresAt", source = "session.expiresAt")
    LoginResult toLoginResult(AuthSession session, String accessToken, String refreshToken, LocalDateTime expiresAt);

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "sessionExpiresAt", source = "session.expiresAt")
    SocialLoginResult toSocialLoginResult(
            AuthSession session,
            String accessToken,
            String refreshToken,
            LocalDateTime expiresAt,
            boolean newUser);

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "sessionExpiresAt", source = "session.expiresAt")
    RefreshAccessTokenResult toRefreshResult(
            AuthSession session,
            String accessToken,
            String refreshToken,
            LocalDateTime expiresAt);

    default LogoutAllResult toLogoutAllResult(int revokedCount) {
        return new LogoutAllResult(revokedCount);
    }
}
