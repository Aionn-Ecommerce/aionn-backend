package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.command.VerifyEmailCommand;
import com.ecommerce.identity.application.dto.user.view.UserActionOutcomeView;

public interface VerifyEmailInputPort {

    UserActionOutcomeView execute(VerifyEmailCommand command);
}


