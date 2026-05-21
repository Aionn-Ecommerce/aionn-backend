package com.aionn.identity.application.mapper;

import com.aionn.identity.application.dto.auth.result.LoginResult;
import com.aionn.identity.application.dto.auth.result.LogoutAllResult;
import com.aionn.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.aionn.identity.application.dto.auth.result.SocialLoginResult;
import com.aionn.identity.domain.model.AuthSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthResultMapper {

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    LoginResult toLoginResult(AuthSession session, String accessToken, String refreshToken);

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    SocialLoginResult toSocialLoginResult(AuthSession session, String accessToken, String refreshToken,
            boolean newUser);

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    RefreshAccessTokenResult toRefreshResult(AuthSession session, String accessToken, String refreshToken);

    default LogoutAllResult toLogoutAllResult(int revokedCount) {
        return new LogoutAllResult(revokedCount);
    }
}

