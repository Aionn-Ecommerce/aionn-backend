package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.query.ListUsersQuery;
import com.ecommerce.identity.application.dto.admin.result.UserListResult;

public interface ListUsersQueryPort {
    UserListResult execute(ListUsersQuery query);
}


