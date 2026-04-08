package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.command.ChangeEmailCommand;
import com.ecommerce.identity.application.dto.user.view.UserActionOutcomeView;

public interface ChangeEmailInputPort {

    UserActionOutcomeView execute(ChangeEmailCommand command);
}


