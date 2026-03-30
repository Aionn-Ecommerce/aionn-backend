package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.RemoveUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.UserRolesResult;

public interface RemoveUserRolesInputPort {
    UserRolesResult execute(RemoveUserRolesCommand command);
}