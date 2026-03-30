package com.ecommerce.identity.application.usecase.kyc;

import com.ecommerce.identity.adapter.rest.mapper.kyc.KycDtoMapper;
import com.ecommerce.identity.application.dto.kyc.KycResult;
import com.ecommerce.identity.application.dto.kyc.UploadKycDocumentCommand;
import com.ecommerce.identity.application.port.in.kyc.UploadKycDocumentInputPort;
import com.ecommerce.identity.application.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadKycDocumentUseCase implements UploadKycDocumentInputPort {

    private final KycService kycService;
    private final KycDtoMapper kycDtoMapper;

    @Override
    @Transactional
    public KycResult execute(UploadKycDocumentCommand command) {
        var entity = kycService.uploadDocument(command.userId(), command.kycId(), command.blobUrl());
        return kycDtoMapper.toKycResult(entity);
    }
}
