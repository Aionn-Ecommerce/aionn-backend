package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.command.RemoveUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.result.UserRolesResult;

public interface RemoveUserRolesInputPort {
    UserRolesResult execute(RemoveUserRolesCommand command);
}
