package com.ecommerce.identity.adapter.rest.mapper.kyc;

import com.ecommerce.identity.adapter.rest.dto.kyc.CreateKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.KycResponse;
import com.ecommerce.identity.adapter.rest.dto.kyc.RejectKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.ReviewKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.UploadKycDocumentRequest;
import com.ecommerce.identity.application.dto.kyc.command.ApproveKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.CreateKycCommand;
import com.ecommerce.identity.application.dto.kyc.query.GetKycQuery;
import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.application.dto.kyc.command.RejectKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.ReviewKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.SubmitKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.UploadKycDocumentCommand;
import com.ecommerce.identity.application.dto.kyc.command.CancelKycCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KycDtoMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "docType", source = "request.docType")
    CreateKycCommand toCreateKycCommand(String userId, CreateKycRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "kycId", source = "kycId")
    @Mapping(target = "blobUrl", source = "request.blobUrl")
    UploadKycDocumentCommand toUploadDocumentCommand(String userId, String kycId, UploadKycDocumentRequest request);

    @Mapping(target = "adminId", source = "adminId")
    @Mapping(target = "kycId", source = "kycId")
    @Mapping(target = "note", source = "request.note")
    ReviewKycCommand toReviewCommand(String adminId, String kycId, ReviewKycRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "kycId", source = "kycId")
    SubmitKycCommand toSubmitKycCommand(String userId, String kycId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "kycId", source = "kycId")
    CancelKycCommand toCancelKycCommand(String userId, String kycId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "kycId", source = "kycId")
    GetKycQuery toGetKycQuery(String userId, String kycId);

    @Mapping(target = "adminId", source = "adminId")
    @Mapping(target = "kycId", source = "kycId")
    ApproveKycCommand toApproveKycCommand(String adminId, String kycId);

    @Mapping(target = "adminId", source = "adminId")
    @Mapping(target = "kycId", source = "kycId")
    @Mapping(target = "reason", source = "request.reason")
    RejectKycCommand toRejectCommand(String adminId, String kycId, RejectKycRequest request);

    KycResponse toResponse(KycResult result);

    List<KycResponse> toResponses(List<KycResult> results);
}
