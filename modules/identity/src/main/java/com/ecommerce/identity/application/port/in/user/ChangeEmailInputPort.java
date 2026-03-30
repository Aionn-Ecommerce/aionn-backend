package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.ChangeEmailCommand;
import com.ecommerce.identity.application.dto.user.UserActionOutcomeView;

public interface ChangeEmailInputPort {

    UserActionOutcomeView execute(ChangeEmailCommand command);
}
