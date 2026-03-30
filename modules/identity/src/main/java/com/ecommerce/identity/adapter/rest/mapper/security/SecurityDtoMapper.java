package com.ecommerce.identity.adapter.rest.mapper.security;

import com.ecommerce.identity.adapter.rest.dto.security.BackupCodesResponse;
import com.ecommerce.identity.adapter.rest.dto.security.ChangePasswordRequest;
import com.ecommerce.identity.adapter.rest.dto.security.CompletePasswordResetRequest;
import com.ecommerce.identity.adapter.rest.dto.security.MfaResponse;
import com.ecommerce.identity.adapter.rest.dto.security.MfaToggleRequest;
import com.ecommerce.identity.adapter.rest.dto.security.PasswordResetRequestCommand;
import com.ecommerce.identity.adapter.rest.dto.security.PasswordResetResponse;
import com.ecommerce.identity.adapter.rest.dto.security.SecurityAuditLogResponse;
import com.ecommerce.identity.application.dto.security.BackupCodesResult;
import com.ecommerce.identity.application.dto.security.ChangePasswordCommand;
import com.ecommerce.identity.application.dto.security.CompletePasswordResetCommand;
import com.ecommerce.identity.application.dto.security.DisableMfaCommand;
import com.ecommerce.identity.application.dto.security.EnableMfaCommand;
import com.ecommerce.identity.application.dto.security.MfaResult;
import com.ecommerce.identity.application.dto.security.PasswordResetResult;
import com.ecommerce.identity.application.dto.security.RegenerateBackupCodesCommand;
import com.ecommerce.identity.application.dto.security.RequestPasswordResetCommand;
import com.ecommerce.identity.application.dto.security.SecurityAuditLogResult;
import com.ecommerce.identity.application.dto.security.UnlockAccountCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SecurityDtoMapper {

    // Request -> Command
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "currentPassword", source = "request.currentPassword")
    @Mapping(target = "newPassword", source = "request.newPassword")
    @Mapping(target = "clientIp", source = "clientIp")
    ChangePasswordCommand toChangePasswordCommand(String userId, String clientIp, ChangePasswordRequest request);

    @Mapping(target = "identity", source = "request.identity")
    @Mapping(target = "clientIp", source = "clientIp")
    RequestPasswordResetCommand toPasswordResetCommand(String clientIp, PasswordResetRequestCommand request);

    @Mapping(target = "token", source = "request.token")
    @Mapping(target = "newPassword", source = "request.newPassword")
    @Mapping(target = "clientIp", source = "clientIp")
    CompletePasswordResetCommand toCompletePasswordResetCommand(String clientIp, CompletePasswordResetRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "password", source = "request.password")
    @Mapping(target = "clientIp", source = "clientIp")
    EnableMfaCommand toEnableMfaCommand(String userId, String clientIp, MfaToggleRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "password", source = "request.password")
    @Mapping(target = "clientIp", source = "clientIp")
    DisableMfaCommand toDisableMfaCommand(String userId, String clientIp, MfaToggleRequest request);

    RegenerateBackupCodesCommand toRegenerateBackupCodesCommand(String userId, String clientIp);

    UnlockAccountCommand toUnlockAccountCommand(String userId);

    // Result -> Response
    @Mapping(target = "resetToken", source = "resetToken")
    PasswordResetResponse toPasswordResetResponse(PasswordResetResult result);

    @Mapping(target = "mfaEnabled", source = "mfaEnabled")
    MfaResponse toMfaResponse(MfaResult result);

    @Mapping(target = "backupCodes", source = "backupCodes")
    BackupCodesResponse toBackupCodesResponse(BackupCodesResult result);

    @Mapping(target = "auditId", source = "auditId")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "deviceId", source = "deviceId")
    @Mapping(target = "timestamp", source = "timestamp")
    SecurityAuditLogResponse toAuditLogRow(SecurityAuditLogResult audit);

    List<SecurityAuditLogResponse> toAuditLogResponse(List<SecurityAuditLogResult> logs);
}
