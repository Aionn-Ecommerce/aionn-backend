package com.aionn.identity.application.port.in.user;

import com.aionn.identity.application.dto.user.command.ChangeEmailCommand;
import com.aionn.identity.application.dto.user.view.UserActionOutcomeView;

public interface ChangeEmailInputPort {

    UserActionOutcomeView execute(ChangeEmailCommand command);
}



