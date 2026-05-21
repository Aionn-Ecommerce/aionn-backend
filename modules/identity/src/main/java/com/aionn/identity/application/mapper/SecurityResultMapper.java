package com.aionn.identity.application.mapper;

import com.aionn.identity.application.dto.security.result.SecurityAuditLogResult;
import com.aionn.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SecurityResultMapper {

    SecurityAuditLogResult toAuditLogResult(SecurityAuditEntity entity);
}
