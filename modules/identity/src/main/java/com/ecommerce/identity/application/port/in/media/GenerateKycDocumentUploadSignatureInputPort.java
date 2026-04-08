package com.ecommerce.identity.application.port.in.media;

import com.ecommerce.identity.application.dto.media.result.UploadSignatureResult;

public interface GenerateKycDocumentUploadSignatureInputPort {
    UploadSignatureResult execute(String userId);
}


