package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.command.RequestDataExportCommand;
import com.ecommerce.identity.application.dto.user.view.DataExportRequestView;

public interface RequestDataExportInputPort {

    DataExportRequestView execute(RequestDataExportCommand command);
}


