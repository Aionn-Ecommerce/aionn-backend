package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.command.UploadKycDocumentCommand;
import com.ecommerce.identity.application.dto.kyc.result.KycResult;

public interface UploadKycDocumentInputPort {
    KycResult execute(UploadKycDocumentCommand command);
}

