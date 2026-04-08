package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.command.UpdateDisplayNameCommand;
import com.ecommerce.identity.application.dto.user.view.UserProfileView;

public interface UpdateDisplayNameInputPort {

    UserProfileView execute(UpdateDisplayNameCommand command);
}



