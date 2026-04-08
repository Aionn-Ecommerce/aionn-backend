package com.ecommerce.identity.adapter.rest.mapper.admin;

import com.ecommerce.identity.adapter.rest.dto.admin.UpdateRolesRequest;
import com.ecommerce.identity.adapter.rest.dto.admin.UpdateUserStatusRequest;
import com.ecommerce.identity.adapter.rest.dto.admin.UserDetailResponse;
import com.ecommerce.identity.adapter.rest.dto.admin.UserRolesResponse;
import com.ecommerce.identity.adapter.rest.dto.admin.UserStatusResponse;
import com.ecommerce.identity.adapter.rest.dto.admin.UserSummaryResponse;
import com.ecommerce.identity.application.dto.admin.command.RemoveUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.command.UpdateUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.command.UpdateUserStatusCommand;
import com.ecommerce.identity.application.dto.admin.query.ListUsersQuery;
import com.ecommerce.identity.application.dto.admin.result.UserDetailResult;
import com.ecommerce.identity.application.dto.admin.result.UserListResult;
import com.ecommerce.identity.application.dto.admin.result.UserRolesResult;
import com.ecommerce.identity.application.dto.admin.result.UserStatusResult;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AdminUserDtoMapperImpl implements AdminUserDtoMapper {

    @Override
    public UpdateUserRolesCommand toUpdateRolesCommand(String userId, UpdateRolesRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        Set<String> roles = null;
        if ( request != null ) {
            roles = userRoleSetToStringSet( request.roles() );
        }
        String userId1 = null;
        userId1 = userId;

        UpdateUserRolesCommand updateUserRolesCommand = new UpdateUserRolesCommand( userId1, roles );

        return updateUserRolesCommand;
    }

    @Override
    public RemoveUserRolesCommand toRemoveRolesCommand(String userId, UpdateRolesRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        Set<String> roles = null;
        if ( request != null ) {
            roles = userRoleSetToStringSet( request.roles() );
        }
        String userId1 = null;
        userId1 = userId;

        RemoveUserRolesCommand removeUserRolesCommand = new RemoveUserRolesCommand( userId1, roles );

        return removeUserRolesCommand;
    }

    @Override
    public UpdateUserStatusCommand toUpdateStatusCommand(String userId, UpdateUserStatusRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String status = null;
        if ( request != null ) {
            if ( request.status() != null ) {
                status = request.status().name();
            }
        }
        String userId1 = null;
        userId1 = userId;

        UpdateUserStatusCommand updateUserStatusCommand = new UpdateUserStatusCommand( userId1, status );

        return updateUserStatusCommand;
    }

    @Override
    public ListUsersQuery toListUsersQuery(String status, String role, int page, int size) {
        if ( status == null && role == null ) {
            return null;
        }

        String status1 = null;
        status1 = status;
        String role1 = null;
        role1 = role;
        int page1 = 0;
        page1 = page;
        int size1 = 0;
        size1 = size;

        ListUsersQuery listUsersQuery = new ListUsersQuery( status1, role1, page1, size1 );

        return listUsersQuery;
    }

    @Override
    public UserRolesResponse toRolesResponse(UserRolesResult result) {
        if ( result == null ) {
            return null;
        }

        Set<UserRole> roles = null;

        roles = stringSetToUserRoleSet( result.roles() );

        UserRolesResponse userRolesResponse = new UserRolesResponse( roles );

        return userRolesResponse;
    }

    @Override
    public UserStatusResponse toStatusResponse(UserStatusResult result) {
        if ( result == null ) {
            return null;
        }

        UserStatus status = null;

        if ( result.status() != null ) {
            status = Enum.valueOf( UserStatus.class, result.status() );
        }

        UserStatusResponse userStatusResponse = new UserStatusResponse( status );

        return userStatusResponse;
    }

    @Override
    public UserSummaryResponse toUserSummaryResponse(UserListResult.UserSummary user) {
        if ( user == null ) {
            return null;
        }

        String userId = null;
        String email = null;
        String displayName = null;
        UserStatus status = null;
        UserRole primaryRole = null;

        userId = user.userId();
        email = user.email();
        displayName = user.displayName();
        if ( user.status() != null ) {
            status = Enum.valueOf( UserStatus.class, user.status() );
        }
        if ( user.primaryRole() != null ) {
            primaryRole = Enum.valueOf( UserRole.class, user.primaryRole() );
        }

        UserSummaryResponse userSummaryResponse = new UserSummaryResponse( userId, email, displayName, status, primaryRole );

        return userSummaryResponse;
    }

    @Override
    public UserDetailResponse toUserDetailResponse(UserDetailResult result) {
        if ( result == null ) {
            return null;
        }

        String userId = null;
        String email = null;
        String phone = null;
        String displayName = null;
        Set<UserRole> roles = null;
        UserStatus status = null;
        LocalDateTime createdAt = null;
        LocalDateTime emailVerifiedAt = null;
        LocalDateTime phoneVerifiedAt = null;

        userId = result.userId();
        email = result.email();
        phone = result.phone();
        displayName = result.displayName();
        roles = stringSetToUserRoleSet( result.roles() );
        if ( result.status() != null ) {
            status = Enum.valueOf( UserStatus.class, result.status() );
        }
        createdAt = result.createdAt();
        emailVerifiedAt = result.emailVerifiedAt();
        phoneVerifiedAt = result.phoneVerifiedAt();

        UserDetailResponse userDetailResponse = new UserDetailResponse( userId, email, phone, displayName, roles, status, createdAt, emailVerifiedAt, phoneVerifiedAt );

        return userDetailResponse;
    }

    protected Set<String> userRoleSetToStringSet(Set<UserRole> set) {
        if ( set == null ) {
            return null;
        }

        Set<String> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( UserRole userRole : set ) {
            set1.add( userRole.name() );
        }

        return set1;
    }

    protected Set<UserRole> stringSetToUserRoleSet(Set<String> set) {
        if ( set == null ) {
            return null;
        }

        Set<UserRole> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( String string : set ) {
            set1.add( Enum.valueOf( UserRole.class, string ) );
        }

        return set1;
    }
}
