package com.ecommerce.identity.adapter.rest.mapper.kyc;

import com.ecommerce.identity.adapter.rest.dto.kyc.CreateKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.KycResponse;
import com.ecommerce.identity.adapter.rest.dto.kyc.RejectKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.ReviewKycRequest;
import com.ecommerce.identity.adapter.rest.dto.kyc.UploadKycDocumentRequest;
import com.ecommerce.identity.application.dto.kyc.command.ApproveKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.CancelKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.CreateKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.RejectKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.ReviewKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.SubmitKycCommand;
import com.ecommerce.identity.application.dto.kyc.command.UploadKycDocumentCommand;
import com.ecommerce.identity.application.dto.kyc.query.GetKycQuery;
import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class KycDtoMapperImpl implements KycDtoMapper {

    @Override
    public CreateKycCommand toCreateKycCommand(String userId, CreateKycRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String docType = null;
        if ( request != null ) {
            docType = request.docType();
        }
        String userId1 = null;
        userId1 = userId;

        CreateKycCommand createKycCommand = new CreateKycCommand( userId1, docType );

        return createKycCommand;
    }

    @Override
    public UploadKycDocumentCommand toUploadDocumentCommand(String userId, String kycId, UploadKycDocumentRequest request) {
        if ( userId == null && kycId == null && request == null ) {
            return null;
        }

        String blobUrl = null;
        if ( request != null ) {
            blobUrl = request.blobUrl();
        }
        String userId1 = null;
        userId1 = userId;
        String kycId1 = null;
        kycId1 = kycId;

        UploadKycDocumentCommand uploadKycDocumentCommand = new UploadKycDocumentCommand( userId1, kycId1, blobUrl );

        return uploadKycDocumentCommand;
    }

    @Override
    public ReviewKycCommand toReviewCommand(String adminId, String kycId, ReviewKycRequest request) {
        if ( adminId == null && kycId == null && request == null ) {
            return null;
        }

        String note = null;
        if ( request != null ) {
            note = request.note();
        }
        String adminId1 = null;
        adminId1 = adminId;
        String kycId1 = null;
        kycId1 = kycId;

        ReviewKycCommand reviewKycCommand = new ReviewKycCommand( adminId1, kycId1, note );

        return reviewKycCommand;
    }

    @Override
    public SubmitKycCommand toSubmitKycCommand(String userId, String kycId) {
        if ( userId == null && kycId == null ) {
            return null;
        }

        String userId1 = null;
        userId1 = userId;
        String kycId1 = null;
        kycId1 = kycId;

        SubmitKycCommand submitKycCommand = new SubmitKycCommand( userId1, kycId1 );

        return submitKycCommand;
    }

    @Override
    public CancelKycCommand toCancelKycCommand(String userId, String kycId) {
        if ( userId == null && kycId == null ) {
            return null;
        }

        String userId1 = null;
        userId1 = userId;
        String kycId1 = null;
        kycId1 = kycId;

        CancelKycCommand cancelKycCommand = new CancelKycCommand( userId1, kycId1 );

        return cancelKycCommand;
    }

    @Override
    public GetKycQuery toGetKycQuery(String userId, String kycId) {
        if ( userId == null && kycId == null ) {
            return null;
        }

        String userId1 = null;
        userId1 = userId;
        String kycId1 = null;
        kycId1 = kycId;

        GetKycQuery getKycQuery = new GetKycQuery( userId1, kycId1 );

        return getKycQuery;
    }

    @Override
    public ApproveKycCommand toApproveKycCommand(String adminId, String kycId) {
        if ( adminId == null && kycId == null ) {
            return null;
        }

        String adminId1 = null;
        adminId1 = adminId;
        String kycId1 = null;
        kycId1 = kycId;

        ApproveKycCommand approveKycCommand = new ApproveKycCommand( adminId1, kycId1 );

        return approveKycCommand;
    }

    @Override
    public RejectKycCommand toRejectCommand(String adminId, String kycId, RejectKycRequest request) {
        if ( adminId == null && kycId == null && request == null ) {
            return null;
        }

        String reason = null;
        if ( request != null ) {
            reason = request.reason();
        }
        String adminId1 = null;
        adminId1 = adminId;
        String kycId1 = null;
        kycId1 = kycId;

        RejectKycCommand rejectKycCommand = new RejectKycCommand( adminId1, kycId1, reason );

        return rejectKycCommand;
    }

    @Override
    public KycResponse toResponse(KycResult result) {
        if ( result == null ) {
            return null;
        }

        String kycId = null;
        String userId = null;
        String docType = null;
        String blobUrl = null;
        String status = null;
        String adminId = null;
        String reason = null;
        LocalDateTime submittedAt = null;
        LocalDateTime approvedAt = null;

        kycId = result.kycId();
        userId = result.userId();
        docType = result.docType();
        blobUrl = result.blobUrl();
        status = result.status();
        adminId = result.adminId();
        reason = result.reason();
        submittedAt = result.submittedAt();
        approvedAt = result.approvedAt();

        KycResponse kycResponse = new KycResponse( kycId, userId, docType, blobUrl, status, adminId, reason, submittedAt, approvedAt );

        return kycResponse;
    }

    @Override
    public List<KycResponse> toResponses(List<KycResult> results) {
        if ( results == null ) {
            return null;
        }

        List<KycResponse> list = new ArrayList<KycResponse>( results.size() );
        for ( KycResult kycResult : results ) {
            list.add( toResponse( kycResult ) );
        }

        return list;
    }
}
