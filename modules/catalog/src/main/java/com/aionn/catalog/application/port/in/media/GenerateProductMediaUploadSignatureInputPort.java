package com.aionn.catalog.application.port.in.media;

import com.aionn.catalog.application.dto.media.result.UploadSignatureResult;

public interface GenerateProductMediaUploadSignatureInputPort {
    UploadSignatureResult execute(String merchantId);

    UploadSignatureResult executeReview(String userId);
}
