package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.command.RequestAccountDeletionCommand;
import com.ecommerce.identity.application.dto.user.view.DeletionRequestView;

public interface RequestAccountDeletionInputPort {

    DeletionRequestView execute(RequestAccountDeletionCommand command);
}



