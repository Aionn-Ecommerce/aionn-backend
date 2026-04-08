package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.command.UpdateAvatarCommand;
import com.ecommerce.identity.application.dto.user.view.UserProfileView;

public interface UpdateAvatarInputPort {

    UserProfileView execute(UpdateAvatarCommand command);
}


