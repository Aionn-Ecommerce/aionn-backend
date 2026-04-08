package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.command.ChangePhoneCommand;
import com.ecommerce.identity.application.dto.user.view.UserActionOutcomeView;

public interface ChangePhoneInputPort {

    UserActionOutcomeView execute(ChangePhoneCommand command);
}



