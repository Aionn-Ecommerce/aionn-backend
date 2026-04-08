package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.kyc.result.KycResult;
import com.ecommerce.identity.domain.model.KycProfile;
import com.ecommerce.identity.infrastructure.persistence.entity.KycProfileEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class KycResultMapperImpl implements KycResultMapper {

    @Override
    public KycResult toResult(KycProfileEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String userId = null;
        String kycId = null;
        String docType = null;
        String blobUrl = null;
        String status = null;
        String adminId = null;
        String reason = null;
        LocalDateTime submittedAt = null;
        LocalDateTime approvedAt = null;

        userId = entityUserUserId( entity );
        kycId = entity.getKycId();
        docType = entity.getDocType();
        blobUrl = entity.getBlobUrl();
        status = entity.getStatus();
        adminId = entity.getAdminId();
        reason = entity.getReason();
        submittedAt = entity.getSubmittedAt();
        approvedAt = entity.getApprovedAt();

        KycResult kycResult = new KycResult( kycId, userId, docType, blobUrl, status, adminId, reason, submittedAt, approvedAt );

        return kycResult;
    }

    @Override
    public KycResult toResult(KycProfile domain) {
        if ( domain == null ) {
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

        kycId = domain.getKycId();
        userId = domain.getUserId();
        docType = domain.getDocType();
        blobUrl = domain.getBlobUrl();
        if ( domain.getStatus() != null ) {
            status = domain.getStatus().name();
        }
        adminId = domain.getAdminId();
        reason = domain.getReason();
        submittedAt = domain.getSubmittedAt();
        approvedAt = domain.getApprovedAt();

        KycResult kycResult = new KycResult( kycId, userId, docType, blobUrl, status, adminId, reason, submittedAt, approvedAt );

        return kycResult;
    }

    private String entityUserUserId(KycProfileEntity kycProfileEntity) {
        UserEntity user = kycProfileEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
