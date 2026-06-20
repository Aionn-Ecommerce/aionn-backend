package com.aionn.identity.application.service;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.valueobject.FeedbackCategory;
import com.aionn.identity.domain.valueobject.FeedbackStatus;
import com.aionn.identity.infrastructure.persistence.entity.UserFeedbackEntity;
import com.aionn.identity.infrastructure.persistence.repository.feedback.UserFeedbackRepository;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {

    private final UserFeedbackRepository repository;

    public UserFeedbackEntity submit(String userId, String category, String subject, String content,
            Integer rating, String contactEmail, String contactPhone) {
        UserFeedbackEntity entity = UserFeedbackEntity.builder()
                .feedbackId(IdGenerator.ulid())
                .userId(userId)
                .category(FeedbackCategory.from(category))
                .subject(trimToNull(subject))
                .content(content.trim())
                .rating(rating == null ? null : rating.shortValue())
                .contactEmail(trimToNull(contactEmail))
                .contactPhone(trimToNull(contactPhone))
                .status(FeedbackStatus.OPEN)
                .build();
        UserFeedbackEntity saved = repository.save(entity);
        log.info("Feedback submitted: id={} userId={} category={}", saved.getFeedbackId(), userId, saved.getCategory());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<UserFeedbackEntity> listMine(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Page<UserFeedbackEntity> adminList(FeedbackStatus status, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize);
        return status != null
                ? repository.findByStatusOrderByCreatedAtDesc(status, pageRequest)
                : repository.findAllByOrderByCreatedAtDesc(pageRequest);
    }

    @Transactional(readOnly = true)
    public UserFeedbackEntity adminGet(String feedbackId) {
        return requireFeedback(feedbackId);
    }

    public UserFeedbackEntity adminReply(String feedbackId, String adminId, String reply,
            FeedbackStatus newStatus) {
        UserFeedbackEntity entity = requireFeedback(feedbackId);
        entity.setAdminReply(reply);
        entity.setHandledBy(adminId);
        entity.setHandledAt(LocalDateTime.now());
        if (newStatus != null) {
            entity.setStatus(newStatus);
        } else if (entity.getStatus() == FeedbackStatus.OPEN) {
            entity.setStatus(FeedbackStatus.IN_REVIEW);
        }
        return repository.save(entity);
    }

    public UserFeedbackEntity adminChangeStatus(String feedbackId, String adminId, FeedbackStatus newStatus) {
        UserFeedbackEntity entity = requireFeedback(feedbackId);
        entity.setStatus(newStatus);
        entity.setHandledBy(adminId);
        entity.setHandledAt(LocalDateTime.now());
        return repository.save(entity);
    }

    private UserFeedbackEntity requireFeedback(String feedbackId) {
        return repository.findById(feedbackId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.FEEDBACK_NOT_FOUND));
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
