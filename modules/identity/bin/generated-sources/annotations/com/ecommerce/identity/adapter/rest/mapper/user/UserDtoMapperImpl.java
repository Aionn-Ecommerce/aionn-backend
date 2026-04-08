package com.ecommerce.identity.adapter.rest.mapper.user;

import com.ecommerce.identity.adapter.rest.dto.user.ChangeAvatarRequest;
import com.ecommerce.identity.adapter.rest.dto.user.ChangeDisplayNameRequest;
import com.ecommerce.identity.adapter.rest.dto.user.ChangeEmailRequest;
import com.ecommerce.identity.adapter.rest.dto.user.ChangePhoneRequest;
import com.ecommerce.identity.adapter.rest.dto.user.DataExportRequestResponse;
import com.ecommerce.identity.adapter.rest.dto.user.DeletionRequestResponse;
import com.ecommerce.identity.adapter.rest.dto.user.UserActionResponse;
import com.ecommerce.identity.adapter.rest.dto.user.UserProfileResponse;
import com.ecommerce.identity.adapter.rest.dto.user.VerifyEmailRequest;
import com.ecommerce.identity.application.dto.user.command.CancelAccountDeletionCommand;
import com.ecommerce.identity.application.dto.user.command.ChangeEmailCommand;
import com.ecommerce.identity.application.dto.user.command.ChangePhoneCommand;
import com.ecommerce.identity.application.dto.user.command.RequestAccountDeletionCommand;
import com.ecommerce.identity.application.dto.user.command.RequestDataExportCommand;
import com.ecommerce.identity.application.dto.user.command.UpdateAvatarCommand;
import com.ecommerce.identity.application.dto.user.command.UpdateDisplayNameCommand;
import com.ecommerce.identity.application.dto.user.command.VerifyEmailCommand;
import com.ecommerce.identity.application.dto.user.query.GetMyProfileQuery;
import com.ecommerce.identity.application.dto.user.view.DataExportRequestView;
import com.ecommerce.identity.application.dto.user.view.DeletionRequestView;
import com.ecommerce.identity.application.dto.user.view.UserProfileView;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserDtoMapperImpl implements UserDtoMapper {

    @Override
    public GetMyProfileQuery toGetMyProfileQuery(String userId) {
        if ( userId == null ) {
            return null;
        }

        String userId1 = null;

        userId1 = userId;

        GetMyProfileQuery getMyProfileQuery = new GetMyProfileQuery( userId1 );

        return getMyProfileQuery;
    }

    @Override
    public VerifyEmailCommand toVerifyEmailCommand(String userId, VerifyEmailRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String otpCode = null;
        if ( request != null ) {
            otpCode = request.otpCode();
        }
        String userId1 = null;
        userId1 = userId;

        String action = request.action().name();

        VerifyEmailCommand verifyEmailCommand = new VerifyEmailCommand( userId1, action, otpCode );

        return verifyEmailCommand;
    }

    @Override
    public UpdateDisplayNameCommand toUpdateDisplayNameCommand(String userId, ChangeDisplayNameRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String displayName = null;
        if ( request != null ) {
            displayName = request.displayName();
        }
        String userId1 = null;
        userId1 = userId;

        UpdateDisplayNameCommand updateDisplayNameCommand = new UpdateDisplayNameCommand( userId1, displayName );

        return updateDisplayNameCommand;
    }

    @Override
    public UpdateAvatarCommand toUpdateAvatarCommand(String userId, ChangeAvatarRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String avatarUrl = null;
        if ( request != null ) {
            avatarUrl = request.avatarUrl();
        }
        String userId1 = null;
        userId1 = userId;

        UpdateAvatarCommand updateAvatarCommand = new UpdateAvatarCommand( userId1, avatarUrl );

        return updateAvatarCommand;
    }

    @Override
    public ChangeEmailCommand toChangeEmailCommand(String userId, ChangeEmailRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String newEmail = null;
        String otpCode = null;
        if ( request != null ) {
            newEmail = request.newEmail();
            otpCode = request.otpCode();
        }
        String userId1 = null;
        userId1 = userId;

        String action = request.action().name();

        ChangeEmailCommand changeEmailCommand = new ChangeEmailCommand( userId1, action, newEmail, otpCode );

        return changeEmailCommand;
    }

    @Override
    public ChangePhoneCommand toChangePhoneCommand(String userId, ChangePhoneRequest request) {
        if ( userId == null && request == null ) {
            return null;
        }

        String newPhone = null;
        String otpCode = null;
        if ( request != null ) {
            newPhone = request.newPhone();
            otpCode = request.otpCode();
        }
        String userId1 = null;
        userId1 = userId;

        String action = request.action().name();

        ChangePhoneCommand changePhoneCommand = new ChangePhoneCommand( userId1, action, newPhone, otpCode );

        return changePhoneCommand;
    }

    @Override
    public RequestAccountDeletionCommand toRequestAccountDeletionCommand(String userId) {
        if ( userId == null ) {
            return null;
        }

        String userId1 = null;

        userId1 = userId;

        RequestAccountDeletionCommand requestAccountDeletionCommand = new RequestAccountDeletionCommand( userId1 );

        return requestAccountDeletionCommand;
    }

    @Override
    public CancelAccountDeletionCommand toCancelAccountDeletionCommand(String userId) {
        if ( userId == null ) {
            return null;
        }

        String userId1 = null;

        userId1 = userId;

        CancelAccountDeletionCommand cancelAccountDeletionCommand = new CancelAccountDeletionCommand( userId1 );

        return cancelAccountDeletionCommand;
    }

    @Override
    public RequestDataExportCommand toRequestDataExportCommand(String userId) {
        if ( userId == null ) {
            return null;
        }

        String userId1 = null;

        userId1 = userId;

        RequestDataExportCommand requestDataExportCommand = new RequestDataExportCommand( userId1 );

        return requestDataExportCommand;
    }

    @Override
    public UserActionResponse toActionResponse(String action) {
        if ( action == null ) {
            return null;
        }

        String status = null;

        status = action;

        UserActionResponse userActionResponse = new UserActionResponse( status );

        return userActionResponse;
    }

    @Override
    public UserProfileResponse toProfileResponse(UserProfileView view) {
        if ( view == null ) {
            return null;
        }

        String userId = null;
        String email = null;
        String phone = null;
        String username = null;
        String displayName = null;
        String avatarUrl = null;
        Set<String> roles = null;
        String status = null;
        LocalDateTime emailVerifiedAt = null;
        LocalDateTime phoneVerifiedAt = null;
        LocalDateTime createdAt = null;

        userId = view.userId();
        email = view.email();
        phone = view.phone();
        username = view.username();
        displayName = view.displayName();
        avatarUrl = view.avatarUrl();
        Set<String> set = view.roles();
        if ( set != null ) {
            roles = new LinkedHashSet<String>( set );
        }
        status = view.status();
        emailVerifiedAt = view.emailVerifiedAt();
        phoneVerifiedAt = view.phoneVerifiedAt();
        createdAt = view.createdAt();

        UserProfileResponse userProfileResponse = new UserProfileResponse( userId, email, phone, username, displayName, avatarUrl, roles, status, emailVerifiedAt, phoneVerifiedAt, createdAt );

        return userProfileResponse;
    }

    @Override
    public DeletionRequestResponse toDeletionRequestResponse(DeletionRequestView view) {
        if ( view == null ) {
            return null;
        }

        String requestId = null;
        String status = null;
        LocalDateTime requestedAt = null;
        LocalDateTime scheduledDeletionAt = null;

        requestId = view.requestId();
        status = view.status();
        requestedAt = view.requestedAt();
        scheduledDeletionAt = view.scheduledDeletionAt();

        DeletionRequestResponse deletionRequestResponse = new DeletionRequestResponse( requestId, status, requestedAt, scheduledDeletionAt );

        return deletionRequestResponse;
    }

    @Override
    public DataExportRequestResponse toDataExportResponse(DataExportRequestView view) {
        if ( view == null ) {
            return null;
        }

        String requestId = null;
        String status = null;
        LocalDateTime requestedAt = null;

        requestId = view.requestId();
        status = view.status();
        requestedAt = view.requestedAt();

        DataExportRequestResponse dataExportRequestResponse = new DataExportRequestResponse( requestId, status, requestedAt );

        return dataExportRequestResponse;
    }
}
