package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.command.UnlinkSocialCommand;

public interface UnlinkSocialInputPort {

    void unlinkSocial(String userId, String provider);

    default void execute(UnlinkSocialCommand command) {
        unlinkSocial(command.userId(), command.provider());
    }
}


