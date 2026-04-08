package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.admin.result.UserDetailResult;
import com.ecommerce.identity.domain.model.IdentityUser;
import java.time.LocalDateTime;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AdminResultMapperImpl implements AdminResultMapper {

    @Override
    public UserDetailResult toUserDetailResult(IdentityUser user) {
        if ( user == null ) {
            return null;
        }

        String email = null;
        String phone = null;
        String displayName = null;
        LocalDateTime createdAt = null;
        LocalDateTime emailVerifiedAt = null;
        LocalDateTime phoneVerifiedAt = null;

        email = user.getEmail();
        phone = user.getPhone();
        displayName = user.getDisplayName();
        createdAt = user.getCreatedAt();
        emailVerifiedAt = user.getEmailVerifiedAt();
        phoneVerifiedAt = user.getPhoneVerifiedAt();

        String userId = user.getId().toString();
        Set<String> roles = mapRoles(user);
        String status = user.getStatus().name();

        UserDetailResult userDetailResult = new UserDetailResult( userId, email, phone, displayName, roles, status, createdAt, emailVerifiedAt, phoneVerifiedAt );

        return userDetailResult;
    }
}
