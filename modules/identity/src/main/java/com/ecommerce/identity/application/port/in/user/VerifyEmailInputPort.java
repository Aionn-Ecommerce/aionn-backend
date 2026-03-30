package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.UserActionOutcomeView;
import com.ecommerce.identity.application.dto.user.VerifyEmailCommand;

public interface VerifyEmailInputPort {

    UserActionOutcomeView execute(VerifyEmailCommand command);
}
