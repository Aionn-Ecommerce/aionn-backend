package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.DataExportRequestView;
import com.ecommerce.identity.application.dto.user.RequestDataExportCommand;

public interface RequestDataExportInputPort {

    DataExportRequestView execute(RequestDataExportCommand command);
}
