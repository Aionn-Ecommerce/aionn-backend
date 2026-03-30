package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.DeletionRequestView;
import com.ecommerce.identity.application.dto.user.RequestAccountDeletionCommand;

public interface RequestAccountDeletionInputPort {

    DeletionRequestView execute(RequestAccountDeletionCommand command);
}
