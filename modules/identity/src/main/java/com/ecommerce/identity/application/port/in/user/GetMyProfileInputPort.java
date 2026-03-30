package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.GetMyProfileQuery;
import com.ecommerce.identity.application.dto.user.UserProfileView;

public interface GetMyProfileInputPort {

    UserProfileView execute(GetMyProfileQuery query);
}
