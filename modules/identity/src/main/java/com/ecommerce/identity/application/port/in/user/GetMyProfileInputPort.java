package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.query.GetMyProfileQuery;
import com.ecommerce.identity.application.dto.user.view.UserProfileView;

public interface GetMyProfileInputPort {

    UserProfileView execute(GetMyProfileQuery query);
}



