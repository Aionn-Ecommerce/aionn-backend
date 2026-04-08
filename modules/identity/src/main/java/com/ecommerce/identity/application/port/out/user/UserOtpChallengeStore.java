package com.ecommerce.identity.application.port.out.user;

import com.ecommerce.identity.application.port.out.user.model.UserOtpChallenge;

import java.util.Optional;

public interface UserOtpChallengeStore {

    void save(UserOtpChallenge challenge);

    Optional<UserOtpChallenge> find(String userId, UserOtpPurpose purpose);

    void delete(String userId, UserOtpPurpose purpose);
}


