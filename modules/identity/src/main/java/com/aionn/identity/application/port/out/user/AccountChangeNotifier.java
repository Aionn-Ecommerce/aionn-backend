package com.aionn.identity.application.port.out.user;

public interface AccountChangeNotifier {

    void notifyEmailChanged(String userId, String oldEmail, String newEmail);

    void notifyPhoneChanged(String userId, String oldPhone, String newPhone);

    void notifyPasswordResetRequested(String userId, String resetToken);

    void notifyPasswordChanged(String userId, String channelHint);
}
