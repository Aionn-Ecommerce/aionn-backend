package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.command.UpdateUserStatusCommand;
import com.ecommerce.identity.application.dto.admin.result.UserStatusResult;

public interface UpdateUserStatusInputPort {
    UserStatusResult execute(UpdateUserStatusCommand command);
}

