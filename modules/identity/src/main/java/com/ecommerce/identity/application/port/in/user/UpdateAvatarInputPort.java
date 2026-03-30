package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.UpdateAvatarCommand;
import com.ecommerce.identity.application.dto.user.UserProfileView;

public interface UpdateAvatarInputPort {

    UserProfileView execute(UpdateAvatarCommand command);
}
