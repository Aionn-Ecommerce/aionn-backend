package com.aionn.chat.application.port.out;

import com.aionn.chat.domain.model.UserBlock;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository {

    UserBlock save(UserBlock block);

    Optional<UserBlock> findActive(String blockerId, String blockedId);

    /** Used by the send path to refuse messages to/from blocked users. */
    boolean exists(String blockerId, String blockedId);

    List<UserBlock> findByBlocker(String blockerId);
}

