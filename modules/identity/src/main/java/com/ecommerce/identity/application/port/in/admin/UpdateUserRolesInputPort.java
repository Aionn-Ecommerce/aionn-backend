package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.UpdateUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.UserRolesResult;

public interface UpdateUserRolesInputPort {
    UserRolesResult execute(UpdateUserRolesCommand command);
}