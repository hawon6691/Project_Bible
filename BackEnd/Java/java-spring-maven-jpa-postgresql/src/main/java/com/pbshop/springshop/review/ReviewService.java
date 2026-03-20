package com.pbshop.springshop.review;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.order.Order;
import com.pbshop.springshop.order.OrderRepository;
import com.pbshop.springshop.order.OrderItem;
import com.pbshop.springshop.point.PointService;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.review.dto.ReviewDtos.CreateReviewRequest;
import com.pbshop.springshop.review.dto.ReviewDtos.UpdateReviewRequest;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PointService pointService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            PointService pointService
    ) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.pointService = pointService;
    }

    public List<Map<String, Object>> getProductReviews(Long productId) {
        ensureProductExists(productId);
        return reviewRepository.findByProductIdOrderByIdDesc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Map<String, Object> createReview(AuthenticatedUserPrincipal principal, Long productId, CreateReviewRequest request) {
        User user = getUser(principal.userId());
        Product product = getProduct(productId);
        Order order = orderRepository.findDetailByIdAndUserId(request.orderId(), principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매 주문을 찾을 수 없습니다."));

        validateOrderContainsProduct(order, productId);

        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(principal.userId(), productId, request.orderId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 작성한 리뷰입니다.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(request.rating());
        review.setContent(request.content());
        Review saved = reviewRepository.save(review);

        recomputeProductReviewStats(product);
        pointService.addPoints(user, "EARN", new BigDecimal("500"), "리뷰 작성 적립", "REVIEW", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public Map<String, Object> updateReview(AuthenticatedUserPrincipal principal, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        requireOwnerOrAdmin(principal, review.getUser().getId());
        review.setRating(request.rating());
        review.setContent(request.content());
        Review saved = reviewRepository.save(review);
        recomputeProductReviewStats(review.getProduct());
        return toResponse(saved);
    }

    @Transactional
    public Map<String, Object> deleteReview(AuthenticatedUserPrincipal principal, Long reviewId) {
        Review review = reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        requireOwnerOrAdmin(principal, review.getUser().getId());
        Product product = review.getProduct();
        reviewRepository.delete(review);
        recomputeProductReviewStats(product);
        return Map.of("message", "리뷰가 삭제되었습니다.");
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private void validateOrderContainsProduct(Order order, Long productId) {
        boolean purchased = order.getItems().stream()
                .map(OrderItem::getProduct)
                .anyMatch(product -> product != null && productId.equals(product.getId()));
        if (!purchased) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "구매한 상품에 대해서만 리뷰를 작성할 수 있습니다.");
        }
    }

    private void requireOwnerOrAdmin(AuthenticatedUserPrincipal principal, Long ownerId) {
        if ("ADMIN".equalsIgnoreCase(principal.role())) {
            return;
        }
        if (!principal.userId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void recomputeProductReviewStats(Product product) {
        long reviewCount = reviewRepository.countByProductId(product.getId());
        BigDecimal average = reviewRepository.findAverageRatingByProductId(product.getId());
        BigDecimal ratingAverage = average == null
                ? BigDecimal.ZERO
                : average.setScale(2, RoundingMode.HALF_UP);
        product.setReviewCount((int) reviewCount);
        product.setRatingAvg(ratingAverage);
        productRepository.save(product);
    }

    private Map<String, Object> toResponse(Review review) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", review.getId());
        response.put("userId", review.getUser().getId());
        response.put("userName", review.getUser().getName());
        response.put("productId", review.getProduct().getId());
        response.put("orderId", review.getOrder().getId());
        response.put("rating", review.getRating());
        response.put("content", review.getContent());
        response.put("createdAt", review.getCreatedAt());
        return response;
    }
}
