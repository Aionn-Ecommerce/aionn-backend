package com.ecommerce.identity.application.port.in.kyc;

import com.ecommerce.identity.application.dto.kyc.UploadKycDocumentCommand;
import com.ecommerce.identity.application.dto.kyc.KycResult;

public interface UploadKycDocumentInputPort {
    KycResult execute(UploadKycDocumentCommand command);
}