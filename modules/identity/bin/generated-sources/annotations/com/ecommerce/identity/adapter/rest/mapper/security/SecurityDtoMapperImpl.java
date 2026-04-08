package com.ecommerce.identity.adapter.rest.mapper.security;

import com.ecommerce.identity.adapter.rest.dto.security.BackupCodesResponse;
import com.ecommerce.identity.adapter.rest.dto.security.ChangePasswordRequest;
import com.ecommerce.identity.adapter.rest.dto.security.CompletePasswordResetRequest;
import com.ecommerce.identity.adapter.rest.dto.security.MfaResponse;
import com.ecommerce.identity.adapter.rest.dto.security.MfaToggleRequest;
import com.ecommerce.identity.adapter.rest.dto.security.PasswordResetRequestCommand;
import com.ecommerce.identity.adapter.rest.dto.security.PasswordResetResponse;
import com.ecommerce.identity.adapter.rest.dto.security.SecurityAuditLogResponse;
import com.ecommerce.identity.application.dto.security.command.ChangePasswordCommand;
import com.ecommerce.identity.application.dto.security.command.CompletePasswordResetCommand;
import com.ecommerce.identity.application.dto.security.command.DisableMfaCommand;
import com.ecommerce.identity.application.dto.security.command.EnableMfaCommand;
import com.ecommerce.identity.application.dto.security.command.RegenerateBackupCodesCommand;
import com.ecommerce.identity.application.dto.security.command.RequestPasswordResetCommand;
import com.ecommerce.identity.application.dto.security.command.UnlockAccountCommand;
import com.ecommerce.identity.application.dto.security.result.BackupCodesResult;
import com.ecommerce.identity.application.dto.security.result.MfaResult;
import com.ecommerce.identity.application.dto.security.result.PasswordResetResult;
import com.ecommerce.identity.application.dto.security.result.SecurityAuditLogResult;
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
public class SecurityDtoMapperImpl implements SecurityDtoMapper {

    @Override
    public ChangePasswordCommand toChangePasswordCommand(String userId, String clientIp, ChangePasswordRequest request) {
        if ( userId == null && clientIp == null && request == null ) {
            return null;
        }

        String currentPassword = null;
        String newPassword = null;
        if ( request != null ) {
            currentPassword = request.currentPassword();
            newPassword = request.newPassword();
        }
        String userId1 = null;
        userId1 = userId;
        String clientIp1 = null;
        clientIp1 = clientIp;

        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand( userId1, currentPassword, newPassword, clientIp1 );

        return changePasswordCommand;
    }

    @Override
    public RequestPasswordResetCommand toPasswordResetCommand(String clientIp, PasswordResetRequestCommand request) {
        if ( clientIp == null && request == null ) {
            return null;
        }

        String identity = null;
        if ( request != null ) {
            identity = request.identity();
        }
        String clientIp1 = null;
        clientIp1 = clientIp;

        RequestPasswordResetCommand requestPasswordResetCommand = new RequestPasswordResetCommand( identity, clientIp1 );

        return requestPasswordResetCommand;
    }

    @Override
    public CompletePasswordResetCommand toCompletePasswordResetCommand(String clientIp, CompletePasswordResetRequest request) {
        if ( clientIp == null && request == null ) {
            return null;
        }

        String token = null;
        String newPassword = null;
        if ( request != null ) {
            token = request.token();
            newPassword = request.newPassword();
        }
        String clientIp1 = null;
        clientIp1 = clientIp;

        CompletePasswordResetCommand completePasswordResetCommand = new CompletePasswordResetCommand( token, newPassword, clientIp1 );

        return completePasswordResetCommand;
    }

    @Override
    public EnableMfaCommand toEnableMfaCommand(String userId, String clientIp, MfaToggleRequest request) {
        if ( userId == null && clientIp == null && request == null ) {
            return null;
        }

        String password = null;
        if ( request != null ) {
            password = request.password();
        }
        String userId1 = null;
        userId1 = userId;
        String clientIp1 = null;
        clientIp1 = clientIp;

        EnableMfaCommand enableMfaCommand = new EnableMfaCommand( userId1, password, clientIp1 );

        return enableMfaCommand;
    }

    @Override
    public DisableMfaCommand toDisableMfaCommand(String userId, String clientIp, MfaToggleRequest request) {
        if ( userId == null && clientIp == null && request == null ) {
            return null;
        }

        String password = null;
        if ( request != null ) {
            password = request.password();
        }
        String userId1 = null;
        userId1 = userId;
        String clientIp1 = null;
        clientIp1 = clientIp;

        DisableMfaCommand disableMfaCommand = new DisableMfaCommand( userId1, password, clientIp1 );

        return disableMfaCommand;
    }

    @Override
    public RegenerateBackupCodesCommand toRegenerateBackupCodesCommand(String userId, String clientIp) {
        if ( userId == null && clientIp == null ) {
            return null;
        }

        String userId1 = null;
        userId1 = userId;
        String clientIp1 = null;
        clientIp1 = clientIp;

        RegenerateBackupCodesCommand regenerateBackupCodesCommand = new RegenerateBackupCodesCommand( userId1, clientIp1 );

        return regenerateBackupCodesCommand;
    }

    @Override
    public UnlockAccountCommand toUnlockAccountCommand(String userId) {
        if ( userId == null ) {
            return null;
        }

        String userId1 = null;

        userId1 = userId;

        UnlockAccountCommand unlockAccountCommand = new UnlockAccountCommand( userId1 );

        return unlockAccountCommand;
    }

    @Override
    public PasswordResetResponse toPasswordResetResponse(PasswordResetResult result) {
        if ( result == null ) {
            return null;
        }

        String resetToken = null;

        resetToken = result.resetToken();

        PasswordResetResponse passwordResetResponse = new PasswordResetResponse( resetToken );

        return passwordResetResponse;
    }

    @Override
    public MfaResponse toMfaResponse(MfaResult result) {
        if ( result == null ) {
            return null;
        }

        boolean mfaEnabled = false;

        mfaEnabled = result.mfaEnabled();

        MfaResponse mfaResponse = new MfaResponse( mfaEnabled );

        return mfaResponse;
    }

    @Override
    public BackupCodesResponse toBackupCodesResponse(BackupCodesResult result) {
        if ( result == null ) {
            return null;
        }

        List<String> backupCodes = null;

        List<String> list = result.backupCodes();
        if ( list != null ) {
            backupCodes = new ArrayList<String>( list );
        }

        BackupCodesResponse backupCodesResponse = new BackupCodesResponse( backupCodes );

        return backupCodesResponse;
    }

    @Override
    public SecurityAuditLogResponse toAuditLogRow(SecurityAuditLogResult audit) {
        if ( audit == null ) {
            return null;
        }

        String auditId = null;
        String eventType = null;
        String description = null;
        String ipAddress = null;
        String deviceId = null;
        LocalDateTime timestamp = null;

        auditId = audit.auditId();
        eventType = audit.eventType();
        description = audit.description();
        ipAddress = audit.ipAddress();
        deviceId = audit.deviceId();
        timestamp = audit.timestamp();

        SecurityAuditLogResponse securityAuditLogResponse = new SecurityAuditLogResponse( auditId, eventType, description, ipAddress, deviceId, timestamp );

        return securityAuditLogResponse;
    }

    @Override
    public List<SecurityAuditLogResponse> toAuditLogResponse(List<SecurityAuditLogResult> logs) {
        if ( logs == null ) {
            return null;
        }

        List<SecurityAuditLogResponse> list = new ArrayList<SecurityAuditLogResponse>( logs.size() );
        for ( SecurityAuditLogResult securityAuditLogResult : logs ) {
            list.add( toAuditLogRow( securityAuditLogResult ) );
        }

        return list;
    }
}
