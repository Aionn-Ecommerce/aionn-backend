package com.aionn.identity.adapter.rest.controller;

import com.aionn.identity.adapter.rest.dto.feedback.request.SubmitFeedbackRequest;
import com.aionn.identity.adapter.rest.dto.feedback.response.FeedbackResponse;
import com.aionn.identity.application.service.FeedbackService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Identity - Feedback", description = "User feedback / contact-us endpoints")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Submit feedback", description = "Public endpoint, accepts both authenticated and anonymous submissions")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submit(
            Authentication authentication,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        String userId = authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
        FeedbackResponse response = feedbackService.submit(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Feedback submitted"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> listMine(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.listMine(authentication.getName()), "Feedbacks fetched"));
    }
}
