package com.aionn.catalog.application.service;

import com.aionn.catalog.application.dto.common.PageResult;
import com.aionn.catalog.application.dto.review.command.HideReviewCommand;
import com.aionn.catalog.application.dto.review.command.MerchantReplyCommand;
import com.aionn.catalog.application.dto.review.command.SubmitReviewCommand;
import com.aionn.catalog.application.dto.review.command.UpdateReviewCommand;
import com.aionn.catalog.application.dto.review.result.RatingSummary;
import com.aionn.catalog.application.dto.review.result.ReviewResult;
import com.aionn.catalog.application.mapper.ReviewResultMapper;
import com.aionn.catalog.application.port.out.MerchantPersistencePort;
import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.application.port.out.ProductReviewPersistencePort;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import com.aionn.catalog.domain.model.Merchant;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductReview;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.catalog.domain.valueobject.ReviewStatus;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.integration.port.ordering.OrderQueryPort;
import com.aionn.sharedkernel.util.IdGenerator;
import com.aionn.sharedkernel.domain.vo.OffsetPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewPersistencePort reviewRepository;
    private final ProductPersistencePort productRepository;
    private final MerchantPersistencePort merchantRepository;
    private final OrderQueryPort orderQueryPort;
    private final ReviewResultMapper resultMapper;
    private final EventPublisher eventPublisher;

    public ReviewResult submitReview(SubmitReviewCommand command) {
        // 1. Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(command.userId(), command.productId())) {
            throw new CatalogException(CatalogErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 2. Retrieve product to get its SKUs
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));

        List<String> skuIds = product.variants().stream()
                .map(ProductVariant::skuId)
                .toList();

        // 3. Verify purchase of any variant
        String orderId = orderQueryPort.findCompletedOrderIdForSkus(command.userId(), skuIds);
        if (orderId == null) {
            throw new CatalogException(CatalogErrorCode.REVIEW_NOT_PURCHASED);
        }

        // 4. Create and save review
        ProductReview review = ProductReview.create(
                IdGenerator.ulid(),
                command.productId(),
                command.userId(),
                orderId,
                command.rating(),
                command.title(),
                command.content(),
                command.imageUrls()
        );

        ProductReview saved = reviewRepository.save(review);
        eventPublisher.publish(review.pullEvents());
        return resultMapper.toResult(saved);
    }

    public ReviewResult updateReview(UpdateReviewCommand command) {
        ProductReview review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUserId().equals(command.userId())) {
            throw new CatalogException(CatalogErrorCode.REVIEW_FORBIDDEN, "User does not own this review");
        }

        review.update(command.rating(), command.title(), command.content(), command.imageUrls());
        ProductReview saved = reviewRepository.save(review);
        eventPublisher.publish(review.pullEvents());
        return resultMapper.toResult(saved);
    }

    public void deleteReview(String userId, String reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUserId().equals(userId)) {
            throw new CatalogException(CatalogErrorCode.REVIEW_FORBIDDEN, "User does not own this review");
        }

        reviewRepository.deleteById(reviewId);
    }

    public ReviewResult merchantReply(MerchantReplyCommand command) {
        ProductReview review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.REVIEW_NOT_FOUND));

        Product product = productRepository.findById(review.getProductId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));

        Merchant merchant = merchantRepository.findByOwnerId(command.ownerId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.MERCHANT_NOT_FOUND,
                        "No merchant registered for the authenticated user"));

        if (!product.getMerchantId().equals(merchant.getMerchantId())) {
            throw new CatalogException(CatalogErrorCode.REVIEW_FORBIDDEN, "Merchant does not own the reviewed product");
        }

        review.reply(command.content());
        ProductReview saved = reviewRepository.save(review);
        eventPublisher.publish(review.pullEvents());
        return resultMapper.toResult(saved);
    }

    public ReviewResult hideReview(HideReviewCommand command) {
        ProductReview review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.REVIEW_NOT_FOUND));

        review.hide();
        ProductReview saved = reviewRepository.save(review);
        eventPublisher.publish(review.pullEvents());
        return resultMapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public PageResult<ReviewResult> getByProduct(String productId, int page, int size) {
        var pagination = OffsetPagination.safe(page, size);
        List<ProductReview> reviews = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.VISIBLE, pagination);
        long total = reviewRepository.countVisibleReviews(productId);
        List<ReviewResult> results = reviews.stream().map(resultMapper::toResult).toList();
        return new PageResult<>(results, page, size, total);
    }

    @Transactional(readOnly = true)
    public PageResult<ReviewResult> getMyReviews(String userId, int page, int size) {
        var pagination = OffsetPagination.safe(page, size);
        List<ProductReview> reviews = reviewRepository.findByUserId(userId, pagination);
        long total = reviewRepository.countByUserId(userId);
        List<ReviewResult> results = reviews.stream().map(resultMapper::toResult).toList();
        return new PageResult<>(results, page, size, total);
    }

    @Transactional(readOnly = true)
    public RatingSummary getProductRatingSummary(String productId) {
        // verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));

        double avg = reviewRepository.getAverageRating(productId);
        long total = reviewRepository.countVisibleReviews(productId);
        Map<Integer, Long> distribution = reviewRepository.getRatingDistribution(productId);

        return new RatingSummary(avg, total, distribution);
    }
}
