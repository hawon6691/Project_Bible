package com.pbshop.springshop.review;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.review.dto.ReviewDtos.CreateReviewRequest;
import com.pbshop.springshop.review.dto.ReviewDtos.UpdateReviewRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}/reviews")
    public ApiResponse<List<Map<String, Object>>> getProductReviews(@PathVariable Long productId) {
        return ApiResponse.success(reviewService.getProductReviews(productId));
    }

    @PostMapping("/products/{productId}/reviews")
    public ApiResponse<Map<String, Object>> createReview(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ApiResponse.success(reviewService.createReview(principal, productId, request));
    }

    @PatchMapping("/reviews/{id}")
    public ApiResponse<Map<String, Object>> updateReview(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return ApiResponse.success(reviewService.updateReview(principal, id, request));
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Map<String, Object>> deleteReview(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(reviewService.deleteReview(principal, id));
    }
}
