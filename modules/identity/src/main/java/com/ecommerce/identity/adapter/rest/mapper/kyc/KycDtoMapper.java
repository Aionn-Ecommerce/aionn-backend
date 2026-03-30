package com.ecommerce.identity.adapter.rest.mapper.kyc;

import com.ecommerce.identity.adapter.rest.dto.kyc.CreateKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.KycResponse;
import com.ecommerce.identity.adapter.rest.dto.kyc.RejectKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.ReviewKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.UploadKycDocumentRequest;
import com.ecommerce.identity.application.dto.kyc.ApproveKycCommand;
import com.ecommerce.identity.application.dto.kyc.CancelKycCommand;
import com.ecommerce.identity.application.dto.kyc.CreateKycCommand;
import com.ecommerce.identity.application.dto.kyc.GetKycQuery;
import com.ecommerce.identity.application.dto.kyc.KycResult;
import com.ecommerce.identity.application.dto.kyc.RejectKycCommand;
import com.ecommerce.identity.application.dto.kyc.ReviewKycCommand;
import com.ecommerce.identity.application.dto.kyc.SubmitKycCommand;
import com.ecommerce.identity.application.dto.kyc.UploadKycDocumentCommand;
import com.ecommerce.identity.infrastructure.persistence.entity.KycProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KycDtoMapper {

    // Request -> Command
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

    SubmitKycCommand toSubmitKycCommand(String userId, String kycId);

    CancelKycCommand toCancelKycCommand(String userId, String kycId);

    GetKycQuery toGetKycQuery(String userId, String kycId);

    ApproveKycCommand toApproveKycCommand(String adminId, String kycId);

    @Mapping(target = "adminId", source = "adminId")
    @Mapping(target = "kycId", source = "kycId")
    @Mapping(target = "reason", source = "request.reason")
    RejectKycCommand toRejectCommand(String adminId, String kycId, RejectKycRequest request);

    // Entity -> Result
    @Mapping(target = "kycId", source = "kycId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "docType", source = "docType")
    @Mapping(target = "blobUrl", source = "blobUrl")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "adminId", source = "adminId")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "submittedAt", source = "submittedAt")
    @Mapping(target = "approvedAt", source = "approvedAt")
    KycResult toKycResult(KycProfileEntity entity);

    List<KycResult> toKycResults(List<KycProfileEntity> entities);

    // Result -> Response
    @Mapping(target = "kycId", source = "kycId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "docType", source = "docType")
    @Mapping(target = "blobUrl", source = "blobUrl")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "adminId", source = "adminId")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "submittedAt", source = "submittedAt")
    @Mapping(target = "approvedAt", source = "approvedAt")
    KycResponse toResponse(KycResult result);

    List<KycResponse> toResponses(List<KycResult> results);
}
