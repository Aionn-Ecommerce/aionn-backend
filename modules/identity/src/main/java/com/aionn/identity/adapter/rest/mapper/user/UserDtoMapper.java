package com.aionn.identity.adapter.rest.mapper.user;

import com.aionn.identity.adapter.rest.dto.user.ChangeAvatarRequest;
import com.aionn.identity.adapter.rest.dto.user.ChangeDisplayNameRequest;
import com.aionn.identity.adapter.rest.dto.user.ChangeEmailRequest;
import com.aionn.identity.adapter.rest.dto.user.ChangePhoneRequest;
import com.aionn.identity.adapter.rest.dto.user.DataExportRequestResponse;
import com.aionn.identity.adapter.rest.dto.user.DeletionRequestResponse;
import com.aionn.identity.adapter.rest.dto.user.UserActionResponse;
import com.aionn.identity.adapter.rest.dto.user.UserProfileResponse;
import com.aionn.identity.adapter.rest.dto.user.VerifyEmailRequest;
import com.aionn.identity.application.dto.user.command.CancelAccountDeletionCommand;
import com.aionn.identity.application.dto.user.command.ChangeEmailCommand;
import com.aionn.identity.application.dto.user.command.ChangePhoneCommand;
import com.aionn.identity.application.dto.user.command.RequestAccountDeletionCommand;
import com.aionn.identity.application.dto.user.command.RequestDataExportCommand;
import com.aionn.identity.application.dto.user.command.UpdateAvatarCommand;
import com.aionn.identity.application.dto.user.command.UpdateDisplayNameCommand;
import com.aionn.identity.application.dto.user.command.VerifyEmailCommand;
import com.aionn.identity.application.dto.user.query.GetMyProfileQuery;
import com.aionn.identity.application.dto.user.view.DataExportRequestView;
import com.aionn.identity.application.dto.user.view.DeletionRequestView;
import com.aionn.identity.application.dto.user.view.UserActionOutcomeView;
import com.aionn.identity.application.dto.user.view.UserProfileView;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    // Query
    GetMyProfileQuery toGetMyProfileQuery(String userId);

    // Request -> Command
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "action", expression = "java(request.action().name())")
    @Mapping(target = "otpCode", source = "request.otpCode")
    VerifyEmailCommand toVerifyEmailCommand(String userId, VerifyEmailRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "displayName", source = "request.displayName")
    UpdateDisplayNameCommand toUpdateDisplayNameCommand(String userId, ChangeDisplayNameRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "avatarUrl", source = "request.avatarUrl")
    UpdateAvatarCommand toUpdateAvatarCommand(String userId, ChangeAvatarRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "action", expression = "java(request.action().name())")
    @Mapping(target = "newEmail", source = "request.newEmail")
    @Mapping(target = "otpCode", source = "request.otpCode")
    ChangeEmailCommand toChangeEmailCommand(String userId, ChangeEmailRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "action", expression = "java(request.action().name())")
    @Mapping(target = "newPhone", source = "request.newPhone")
    @Mapping(target = "otpCode", source = "request.otpCode")
    ChangePhoneCommand toChangePhoneCommand(String userId, ChangePhoneRequest request);

    RequestAccountDeletionCommand toRequestAccountDeletionCommand(String userId);

    CancelAccountDeletionCommand toCancelAccountDeletionCommand(String userId);

    RequestDataExportCommand toRequestDataExportCommand(String userId);

    // View -> Response
    @Mapping(target = "status", source = "action")
    UserActionResponse toActionResponse(String action);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "emailVerifiedAt", source = "emailVerifiedAt")
    @Mapping(target = "phoneVerifiedAt", source = "phoneVerifiedAt")
    @Mapping(target = "createdAt", source = "createdAt")
    UserProfileResponse toProfileResponse(UserProfileView view);

    @Mapping(target = "requestId", source = "requestId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "requestedAt", source = "requestedAt")
    @Mapping(target = "scheduledDeletionAt", source = "scheduledDeletionAt")
    DeletionRequestResponse toDeletionRequestResponse(DeletionRequestView view);

    @Mapping(target = "requestId", source = "requestId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "requestedAt", source = "requestedAt")
    DataExportRequestResponse toDataExportResponse(DataExportRequestView view);

    // Helper method
    default Object toOutcomeResponse(UserActionOutcomeView outcome) {
        if (outcome.profile() != null) {
            return toProfileResponse(outcome.profile());
        }
        return toActionResponse(outcome.action());
    }
}


