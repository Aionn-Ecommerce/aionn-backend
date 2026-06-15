package com.aionn.identity.application.service;

import com.aionn.identity.adapter.rest.dto.feedback.request.SubmitFeedbackRequest;
import com.aionn.identity.adapter.rest.dto.feedback.response.FeedbackResponse;
import com.aionn.identity.domain.valueobject.FeedbackCategory;
import com.aionn.identity.domain.valueobject.FeedbackStatus;
import com.aionn.identity.infrastructure.persistence.entity.UserFeedbackEntity;
import com.aionn.identity.infrastructure.persistence.repository.feedback.UserFeedbackRepository;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {

    private final UserFeedbackRepository repository;

    public FeedbackResponse submit(String userId, SubmitFeedbackRequest request) {
        UserFeedbackEntity entity = UserFeedbackEntity.builder()
                .feedbackId(IdGenerator.ulid())
                .userId(userId)
                .category(FeedbackCategory.from(request.category()))
                .subject(trimToNull(request.subject()))
                .content(request.content().trim())
                .rating(request.rating() == null ? null : request.rating().shortValue())
                .contactEmail(trimToNull(request.contactEmail()))
                .contactPhone(trimToNull(request.contactPhone()))
                .status(FeedbackStatus.OPEN)
                .build();
        UserFeedbackEntity saved = repository.save(entity);
        log.info("Feedback submitted: id={} userId={} category={}", saved.getFeedbackId(), userId, saved.getCategory());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listMine(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FeedbackService::toResponse)
                .toList();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static FeedbackResponse toResponse(UserFeedbackEntity entity) {
        return new FeedbackResponse(
                entity.getFeedbackId(),
                entity.getUserId(),
                entity.getCategory().name(),
                entity.getSubject(),
                entity.getContent(),
                entity.getRating() == null ? null : entity.getRating().intValue(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getStatus().name(),
                entity.getCreatedAt());
    }
}
