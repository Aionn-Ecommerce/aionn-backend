package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.GetUserQuery;
import com.ecommerce.identity.application.dto.admin.UserDetailResult;

public interface GetUserByIdQueryPort {
    UserDetailResult execute(GetUserQuery query);
}
