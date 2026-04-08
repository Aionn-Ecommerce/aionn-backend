package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class IdentityUserMapperImpl implements IdentityUserMapper {

    @Override
    public IdentityUser toDomain(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserId id = null;
        Set<UserRole> roles = null;
        String email = null;
        String phone = null;
        String username = null;
        String passwordHash = null;
        String displayName = null;
        String avatarUrl = null;
        UserStatus status = null;
        LocalDateTime emailVerifiedAt = null;
        LocalDateTime phoneVerifiedAt = null;
        LocalDateTime createdAt = null;

        id = map( entity.getUserId() );
        Set<UserRole> set = entity.getRoles();
        if ( set != null ) {
            roles = new LinkedHashSet<UserRole>( set );
        }
        email = entity.getEmail();
        phone = entity.getPhone();
        username = entity.getUsername();
        passwordHash = entity.getPasswordHash();
        displayName = entity.getDisplayName();
        avatarUrl = entity.getAvatarUrl();
        status = entity.getStatus();
        emailVerifiedAt = entity.getEmailVerifiedAt();
        phoneVerifiedAt = entity.getPhoneVerifiedAt();
        createdAt = entity.getCreatedAt();

        IdentityUser identityUser = new IdentityUser( id, email, phone, username, passwordHash, displayName, avatarUrl, roles, status, emailVerifiedAt, phoneVerifiedAt, createdAt );

        identityUser.setLockedUntil( entity.getLockedUntil() );

        return identityUser;
    }

    @Override
    public UserEntity toEntity(IdentityUser user) {
        if ( user == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        UUID value = userIdValue( user );
        if ( value != null ) {
            userEntity.setUserId( value.toString() );
        }
        userEntity.setEmail( user.getEmail() );
        userEntity.setPhone( user.getPhone() );
        userEntity.setUsername( user.getUsername() );
        userEntity.setPasswordHash( user.getPasswordHash() );
        userEntity.setDisplayName( user.getDisplayName() );
        userEntity.setAvatarUrl( user.getAvatarUrl() );
        Set<UserRole> set = user.getRoles();
        if ( set != null ) {
            userEntity.setRoles( new LinkedHashSet<UserRole>( set ) );
        }
        userEntity.setStatus( user.getStatus() );
        userEntity.setEmailVerifiedAt( user.getEmailVerifiedAt() );
        userEntity.setPhoneVerifiedAt( user.getPhoneVerifiedAt() );
        userEntity.setCreatedAt( user.getCreatedAt() );

        return userEntity;
    }

    private UUID userIdValue(IdentityUser identityUser) {
        UserId id = identityUser.getId();
        if ( id == null ) {
            return null;
        }
        return id.getValue();
    }
}
