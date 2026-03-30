package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.UpdateDisplayNameCommand;
import com.ecommerce.identity.application.dto.user.UserProfileView;

public interface UpdateDisplayNameInputPort {

    UserProfileView execute(UpdateDisplayNameCommand command);
}
