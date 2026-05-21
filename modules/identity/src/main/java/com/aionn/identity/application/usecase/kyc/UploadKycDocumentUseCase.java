package com.aionn.identity.application.usecase.kyc;

import com.aionn.identity.application.dto.kyc.result.KycResult;
import com.aionn.identity.application.dto.kyc.command.UploadKycDocumentCommand;
import com.aionn.identity.application.mapper.KycResultMapper;
import com.aionn.identity.application.port.in.kyc.UploadKycDocumentInputPort;
import com.aionn.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadKycDocumentUseCase implements UploadKycDocumentInputPort {

    private final KycService kycService;
    private final KycResultMapper kycResultMapper;

    @Override
    @Transactional
    public KycResult execute(UploadKycDocumentCommand command) {
        var entity = kycService.uploadDocument(command.userId(), command.kycId(), command.blobUrl());
        return kycResultMapper.toResult(entity);
    }
}

