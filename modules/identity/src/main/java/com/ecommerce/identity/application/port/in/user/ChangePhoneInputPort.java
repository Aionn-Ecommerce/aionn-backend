package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.ChangePhoneCommand;
import com.ecommerce.identity.application.dto.user.UserActionOutcomeView;

public interface ChangePhoneInputPort {

    UserActionOutcomeView execute(ChangePhoneCommand command);
}
