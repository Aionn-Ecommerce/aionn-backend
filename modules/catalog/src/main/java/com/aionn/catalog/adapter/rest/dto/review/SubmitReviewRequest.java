package com.aionn.catalog.adapter.rest.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public record SubmitReviewRequest(
        @Min(1) @Max(5) int rating,
        String title,
        String content,
        List<String> imageUrls) {
}
