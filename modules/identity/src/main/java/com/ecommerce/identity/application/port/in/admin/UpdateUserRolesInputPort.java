package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.command.UpdateUserRolesCommand;
import com.ecommerce.identity.application.dto.admin.result.UserRolesResult;

public interface UpdateUserRolesInputPort {
    UserRolesResult execute(UpdateUserRolesCommand command);
}

