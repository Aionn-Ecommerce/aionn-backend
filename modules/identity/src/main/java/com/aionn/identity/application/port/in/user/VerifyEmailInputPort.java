package com.aionn.identity.application.port.in.user;

import com.aionn.identity.application.dto.user.command.VerifyEmailCommand;
import com.aionn.identity.application.dto.user.view.UserActionOutcomeView;

public interface VerifyEmailInputPort {

    UserActionOutcomeView execute(VerifyEmailCommand command);
}



