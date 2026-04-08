package com.ecommerce.identity.infrastructure.persistence.mapper;

import com.ecommerce.identity.domain.model.KycProfile;
import com.ecommerce.identity.domain.valueobject.KycStatus;
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
public class KycDomainMapperImpl implements KycDomainMapper {

    @Override
    public KycProfileEntity toEntity(KycProfile domain) {
        if ( domain == null ) {
            return null;
        }

        KycProfileEntity.KycProfileEntityBuilder kycProfileEntity = KycProfileEntity.builder();

        kycProfileEntity.adminId( domain.getAdminId() );
        kycProfileEntity.approvedAt( domain.getApprovedAt() );
        kycProfileEntity.blobUrl( domain.getBlobUrl() );
        kycProfileEntity.docType( domain.getDocType() );
        kycProfileEntity.kycId( domain.getKycId() );
        kycProfileEntity.reason( domain.getReason() );
        kycProfileEntity.submittedAt( domain.getSubmittedAt() );

        kycProfileEntity.status( domain.getStatus().name() );

        return kycProfileEntity.build();
    }

    @Override
    public KycProfile toDomain(KycProfileEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String userId = null;
        String kycId = null;
        String docType = null;
        String blobUrl = null;
        String adminId = null;
        String reason = null;
        LocalDateTime submittedAt = null;
        LocalDateTime approvedAt = null;

        userId = entityUserUserId( entity );
        kycId = entity.getKycId();
        docType = entity.getDocType();
        blobUrl = entity.getBlobUrl();
        adminId = entity.getAdminId();
        reason = entity.getReason();
        submittedAt = entity.getSubmittedAt();
        approvedAt = entity.getApprovedAt();

        KycStatus status = KycStatus.valueOf(entity.getStatus());
        LocalDateTime createdAt = null;

        KycProfile kycProfile = new KycProfile( kycId, userId, docType, blobUrl, status, adminId, reason, submittedAt, approvedAt, createdAt );

        return kycProfile;
    }

    private String entityUserUserId(KycProfileEntity kycProfileEntity) {
        UserEntity user = kycProfileEntity.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }
}
