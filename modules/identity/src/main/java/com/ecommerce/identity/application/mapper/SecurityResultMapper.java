package com.ecommerce.identity.application.mapper;

import com.ecommerce.identity.application.dto.security.result.SecurityAuditLogResult;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SecurityResultMapper {

    SecurityAuditLogResult toAuditLogResult(SecurityAuditEntity entity);
}