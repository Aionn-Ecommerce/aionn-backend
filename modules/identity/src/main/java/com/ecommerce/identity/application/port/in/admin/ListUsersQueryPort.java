package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.ListUsersQuery;
import com.ecommerce.identity.application.dto.admin.UserListResult;

public interface ListUsersQueryPort {
    UserListResult execute(ListUsersQuery query);
}
