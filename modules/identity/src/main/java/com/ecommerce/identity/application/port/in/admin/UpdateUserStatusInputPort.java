package com.ecommerce.identity.application.port.in.admin;

import com.ecommerce.identity.application.dto.admin.UpdateUserStatusCommand;
import com.ecommerce.identity.application.dto.admin.UserStatusResult;

public interface UpdateUserStatusInputPort {
    UserStatusResult execute(UpdateUserStatusCommand command);
}