package com.aionn.identity.infrastructure.persistence.repository.feedback;

import com.aionn.identity.domain.valueobject.FeedbackStatus;
import com.aionn.identity.infrastructure.persistence.entity.UserFeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFeedbackRepository extends JpaRepository<UserFeedbackEntity, String> {

    List<UserFeedbackEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<UserFeedbackEntity> findByStatusOrderByCreatedAtDesc(FeedbackStatus status, Pageable pageable);

    Page<UserFeedbackEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
