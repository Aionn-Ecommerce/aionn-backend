package com.aionn.identity.application.port.in.user;

import com.aionn.identity.application.dto.user.command.ChangePhoneCommand;
import com.aionn.identity.application.dto.user.view.UserActionOutcomeView;

public interface ChangePhoneInputPort {

    UserActionOutcomeView execute(ChangePhoneCommand command);
}




