package com.aionn.identity.application.port.in.kyc;

import com.aionn.identity.application.dto.kyc.command.UploadKycDocumentCommand;
import com.aionn.identity.application.dto.kyc.result.KycResult;

public interface UploadKycDocumentInputPort {
    KycResult execute(UploadKycDocumentCommand command);
}


