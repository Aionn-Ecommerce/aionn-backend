package com.aionn.catalog.application.port.out;

import com.aionn.catalog.domain.model.UserBrowsingHistory;
import java.util.Optional;

public interface UserBrowsingHistoryPersistencePort {
    UserBrowsingHistory save(UserBrowsingHistory history);
    Optional<UserBrowsingHistory> findByUserId(String userId);
}
