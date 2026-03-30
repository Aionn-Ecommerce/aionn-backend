package com.ecommerce.identity.adapter.rest.mapper.admin;

import com.ecommerce.identity.adapter.rest.dto.admin.UpdateRolesRequest;
import com.ecommerce.identity.adapter.rest.dto.admin.UpdateUserStatusRequest;
import com.ecommerce.identity.adapter.rest.dto.admin.UserDetailResponse;
import com.ecommerce.identity.adapter.rest.dto.admin.UserRolesResponse;
import com.ecommerce.identity.adapter.rest.dto.admin.UserStatusResponse;
import com.ecommerce.identity.adapter.rest.dto.admin.UserSummaryResponse;
import com.ecommerce.identity.application.dto.admin.GetUserQuery;
import com.ecommerce.identity.application.dto.admin.ListUsersQuery;
import com.ecommerce.identity.application.dto.admin.RemoveUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.UpdateUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.UpdateUserStatusCommand;
import com.ecommerce.identity.application.dto.admin.UserDetailResult;
import com.ecommerce.identity.application.dto.admin.UserListResult;
import com.ecommerce.identity.application.dto.admin.UserRolesResult;
import com.ecommerce.identity.application.dto.admin.UserStatusResult;
import com.ecommerce.sharedkernel.adapter.web.response.PageMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminUserDtoMapper {

    // Request -> Command
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "roles", source = "request.roles")
    UpdateUserRolesCommand toUpdateRolesCommand(String userId, UpdateRolesRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "roles", source = "request.roles")
    RemoveUserRolesCommand toRemoveRolesCommand(String userId, UpdateRolesRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "status", source = "request.status")
    UpdateUserStatusCommand toUpdateStatusCommand(String userId, UpdateUserStatusRequest request);

    // Query
    ListUsersQuery toListUsersQuery(String status, String role, int page, int size);

    GetUserQuery toGetUserQuery(String userId);

    // Result -> Response
    @Mapping(target = "roles", source = "roles")
    UserRolesResponse toRolesResponse(UserRolesResult result);

    @Mapping(target = "status", source = "status")
    UserStatusResponse toStatusResponse(UserStatusResult result);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "primaryRole", source = "primaryRole")
    UserSummaryResponse toUserSummaryResponse(UserListResult.UserSummary user);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "emailVerifiedAt", source = "emailVerifiedAt")
    @Mapping(target = "phoneVerifiedAt", source = "phoneVerifiedAt")
    UserDetailResponse toUserDetailResponse(UserDetailResult result);

    // Helper methods
    default List<UserSummaryResponse> toUserSummaryResponses(UserListResult result) {
        return result.users().stream()
                .map(this::toUserSummaryResponse)
                .toList();
    }

    default PageMetadata toUserListPaging(UserListResult result) {
        int totalPages = result.size() > 0
                ? (int) Math.ceil((double) result.total() / result.size())
                : 0;
        return new PageMetadata(
                result.page(),
                result.size(),
                result.total(),
                totalPages);
    }
}
