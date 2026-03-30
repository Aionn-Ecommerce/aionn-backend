package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.CancelAccountDeletionCommand;

public interface CancelAccountDeletionInputPort {

    void execute(CancelAccountDeletionCommand command);
}
