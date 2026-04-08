package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.query.GetUserQuery;
import com.ecommerce.identity.application.dto.admin.result.UserDetailResult;

public interface GetUserByIdQueryPort {
    UserDetailResult execute(GetUserQuery query);
}
