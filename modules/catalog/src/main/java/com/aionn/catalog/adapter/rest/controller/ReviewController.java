package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.review.MerchantReplyRequest;
import com.aionn.catalog.adapter.rest.dto.review.SubmitReviewRequest;
import com.aionn.catalog.adapter.rest.dto.review.UpdateReviewRequest;
import com.aionn.catalog.adapter.rest.support.session.CurrentAdminId;
import com.aionn.catalog.adapter.rest.support.session.CurrentOwnerId;
import com.aionn.catalog.application.dto.common.PageResult;
import com.aionn.catalog.application.dto.review.command.HideReviewCommand;
import com.aionn.catalog.application.dto.review.command.MerchantReplyCommand;
import com.aionn.catalog.application.dto.review.command.SubmitReviewCommand;
import com.aionn.catalog.application.dto.review.command.UpdateReviewCommand;
import com.aionn.catalog.application.dto.review.result.RatingSummary;
import com.aionn.catalog.application.dto.review.result.ReviewResult;
import com.aionn.catalog.application.service.ReviewService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog - Review & Rating", description = "Product review submission, update, reply and aggregate rating statistics")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit product review", description = "Submit a rating (1-5) and review for a purchased product")
    public ResponseEntity<ApiResponse<ReviewResult>> submitReview(
            @CurrentOwnerId String userId,
            @PathVariable String productId,
            @Valid @RequestBody SubmitReviewRequest request) {
        ReviewResult result = reviewService.submitReview(new SubmitReviewCommand(
                productId,
                userId,
                request.rating(),
                request.title(),
                request.content(),
                request.imageUrls()
        ));
        return ApiResponse.createdResponse("Review submitted successfully", result);
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update review", description = "Update rating, title, content or images of a review owned by current user")
    public ResponseEntity<ApiResponse<ReviewResult>> updateReview(
            @CurrentOwnerId String userId,
            @PathVariable String reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResult result = reviewService.updateReview(new UpdateReviewCommand(
                reviewId,
                userId,
                request.rating(),
                request.title(),
                request.content(),
                request.imageUrls()
        ));
        return ResponseEntity.ok(ApiResponse.success(result, "Review updated successfully"));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete review", description = "Delete a review owned by current user")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @CurrentOwnerId String userId,
            @PathVariable String reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully"));
    }

    @GetMapping("/products/{productId}/reviews")
    @Operation(summary = "Get reviews of a product", description = "Fetch visible reviews of a product with pagination")
    public ResponseEntity<ApiResponse<PageResult<ReviewResult>>> getByProduct(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<ReviewResult> result = reviewService.getByProduct(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Reviews fetched successfully"));
    }

    @GetMapping("/products/{productId}/reviews/summary")
    @Operation(summary = "Get rating summary of a product", description = "Get rating distribution and average rating for a product")
    public ResponseEntity<ApiResponse<RatingSummary>> getProductRatingSummary(
            @PathVariable String productId) {
        RatingSummary result = reviewService.getProductRatingSummary(productId);
        return ResponseEntity.ok(ApiResponse.success(result, "Rating summary fetched successfully"));
    }

    @GetMapping("/reviews/mine")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user reviews", description = "Retrieve reviews submitted by the authenticated user")
    public ResponseEntity<ApiResponse<PageResult<ReviewResult>>> getMyReviews(
            @CurrentOwnerId String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<ReviewResult> result = reviewService.getMyReviews(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "My reviews fetched successfully"));
    }

    @PostMapping("/reviews/{reviewId}/reply")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Merchant reply to review", description = "Submit merchant response to a product review")
    public ResponseEntity<ApiResponse<ReviewResult>> merchantReply(
            @CurrentOwnerId String ownerId,
            @PathVariable String reviewId,
            @Valid @RequestBody MerchantReplyRequest request) {
        ReviewResult result = reviewService.merchantReply(new MerchantReplyCommand(
                reviewId,
                ownerId,
                request.content()
        ));
        return ResponseEntity.ok(ApiResponse.success(result, "Reply submitted successfully"));
    }

    @GetMapping("/products/{productId}/reviews/eligibility")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check review eligibility", description = "Check whether the authenticated user can submit a review for this product")
    public ResponseEntity<ApiResponse<ReviewService.ReviewEligibility>> checkEligibility(
            @CurrentOwnerId String userId,
            @PathVariable String productId) {
        ReviewService.ReviewEligibility result = reviewService.checkEligibility(
                userId, productId);
        return ResponseEntity.ok(ApiResponse.success(result, "Eligibility checked"));
    }

    @PostMapping("/reviews/{reviewId}/hide")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Hide review (Admin)", description = "Admin can moderate and hide a review")
    public ResponseEntity<ApiResponse<ReviewResult>> hideReview(
            @CurrentAdminId String adminId,
            @PathVariable String reviewId) {
        ReviewResult result = reviewService.hideReview(new HideReviewCommand(
                reviewId,
                adminId
        ));
        return ResponseEntity.ok(ApiResponse.success(result, "Review hidden successfully"));
    }
}
