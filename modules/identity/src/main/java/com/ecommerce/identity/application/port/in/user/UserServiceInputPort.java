package com.ecommerce.identity.application.port.in.user;

import com.ecommerce.identity.application.dto.user.DataExportRequestView;
import com.ecommerce.identity.application.dto.user.DeletionRequestView;
import com.ecommerce.identity.application.dto.user.UserProfileView;

public interface UserServiceInputPort {

    UserProfileView getMyProfile(String userId);

    void sendVerifyPrimaryEmailOtp(String userId);

    void confirmVerifyPrimaryEmailOtp(String userId, String otpCode);

    UserProfileView updateDisplayName(String userId, String displayName);

    UserProfileView updateAvatar(String userId, String avatarUrl);

    void requestEmailChangeOtp(String userId, String newEmail);

    UserProfileView confirmEmailChange(String userId, String otpCode);

    void requestPhoneChangeOtp(String userId, String newPhone);

    UserProfileView confirmPhoneChange(String userId, String otpCode);

    DeletionRequestView requestAccountDeletion(String userId);

    void cancelAccountDeletion(String userId);

    DataExportRequestView requestDataExport(String userId);
}
