package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.auth.result.LoginResult;
import com.ecommerce.identity.application.dto.auth.result.LogoutAllResult;
import com.ecommerce.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;
import com.ecommerce.identity.domain.model.AuthSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthResultMapper {

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    LoginResult toLoginResult(AuthSession session, String accessToken);

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    SocialLoginResult toSocialLoginResult(AuthSession session, String accessToken, boolean newUser);

    @Mapping(target = "userId", source = "session.userId")
    @Mapping(target = "sessionId", source = "session.sessionId")
    @Mapping(target = "expiresAt", source = "session.expiresAt")
    RefreshAccessTokenResult toRefreshResult(AuthSession session, String accessToken);

    default LogoutAllResult toLogoutAllResult(int revokedCount) {
        return new LogoutAllResult(revokedCount);
    }
}
