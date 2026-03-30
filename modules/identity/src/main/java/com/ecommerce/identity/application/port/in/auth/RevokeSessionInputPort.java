package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.RevokeSessionCommand;

public interface RevokeSessionInputPort {

    void execute(RevokeSessionCommand command);
}
