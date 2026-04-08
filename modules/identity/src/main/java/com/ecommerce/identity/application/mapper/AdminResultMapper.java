package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.admin.result.UserDetailResult;
import com.ecommerce.identity.application.dto.admin.result.UserListResult;
import com.ecommerce.identity.application.dto.admin.result.UserRolesResult;
import com.ecommerce.identity.application.dto.admin.result.UserStatusResult;
import com.ecommerce.identity.domain.model.IdentityUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AdminResultMapper {

    @Mapping(target = "userId", expression = "java(user.getId().toString())")
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserDetailResult toUserDetailResult(IdentityUser user);

    default Set<String> mapRoles(IdentityUser user) {
        return user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    default UserRolesResult toUserRolesResult(Set<String> roles) {
        return new UserRolesResult(roles);
    }

    default UserStatusResult toUserStatusResult(String status) {
        return new UserStatusResult(status);
    }

    default UserListResult toUserListResult(List<UserListResult.UserSummary> users, int page, int size, int total) {
        return new UserListResult(users, page, size, total);
    }
}
